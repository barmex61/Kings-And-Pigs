package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.github.quillraven.fleks.*
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import com.fatih.hoghavoc.actors.FlipImage
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.ui.Drawables
import ktx.box2d.body
import ktx.box2d.box
import ktx.math.random
import ktx.math.vec2
import ktx.scene2d.Scene2DSkin
import com.fatih.hoghavoc.ui.get
import com.fatih.hoghavoc.utils.*
import ktx.math.plus
import ktx.math.times
import kotlin.experimental.or

@AllOf([DestructableComponent::class])
class DestructableItemsSystem(
    private val destructableComps : ComponentMapper<DestructableComponent>,
    private val physicWorld : World,
    private val animComps : ComponentMapper<AnimationComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val textureAtlas: TextureAtlas,
    private val gameStage : Stage
) : IteratingSystem(interval = Fixed(1/60f)){

    private lateinit var destructableComponent: DestructableComponent


    override fun onTickEntity(entity: Entity) {

    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        destructableComponent = destructableComps[entity]
        destructableComponent.run {
            boxPiecesBody?.let {
                it.forEach { (flipImage, body) ->
                    body.let {
                        flipImage.run {
                            setPosition(body.position.x - width /2f ,body.position.y - height/2f)
                            rotation = MathUtils.radiansToDegrees * body.angle
                        }
                    }
                }
                boxPiecesDestructTimer -= deltaTime
                if (boxPiecesDestructTimer <= 0f) {
                    it.keys.forEach {
                        gameStage.root.removeActor(it)
                    }
                    it.values.forEach {body->
                        physicWorld.destroyBody(body)
                    }
                    boxPiecesBody = null
                    world.remove(entity)
                }
            }

            if (create) {
                val position = body!!.position
                configureEntity(entity){
                    if (entity in animComps){
                        animComps.remove(entity)
                    }
                    if (entity in imageComps){
                        imageComps.remove(entity)
                    }
                    physicComps.getOrNull(entity)?.let {
                        physicWorld.destroyBody(it.body)
                    }
                }
                val posList = arrayOf(
                    Vector2(0.5f, 0.5f),
                    Vector2(-0.5f, 0.5f),
                    Vector2(-0.5f, -0.5f),
                    Vector2(0.5f, -0.5f)
                )
                (0..3).forEach { index ->
                    if (boxPiecesBody == null) boxPiecesBody = hashMapOf()
                    boxPiecesBody!![FlipImage(textureAtlas.findRegion("box_pieces", index+1)).apply {
                        setSize(0.4f, 0.4f)
                        setOrigin(Align.center)
                        setPosition(position.x + posList[index].x , position.y + posList[index].y)
                    }.also {
                        gameStage.addActor(it)
                    }] = physicWorld.body(BodyDef.BodyType.DynamicBody) {
                        this.position.set(position + posList[index])
                        box(0.2f, 0.2f ) {
                            isSensor = true
                            friction = 0f
                            restitution = 0f
                            filter.categoryBits = -5
                        }
                    }.also { body ->
                        body.applyLinearImpulse(
                            posList[index] * (-3f..3f).random(),
                            body.worldCenter,
                            true
                        )
                    }
                }
                create = false
                createItemBodies(position)
            }
        }
    }



    private fun DestructableComponent.createItemBodies(bodyPos : Vector2){
        itemModels.forEach { itemModel ->

            world.entity {itemEntity->

                add<ImageComponent>{
                    image = FlipImage().apply {
                        drawable = Scene2DSkin.defaultSkin[Drawables.BIG_HEARTH]
                        if (itemModel == EntityModel.BOMB) setSize(3f,3f) else setSize(0.7f,0.7f)
                        setPosition(bodyPos.x,bodyPos.y)
                    }
                }

                add<AnimationComponent>{
                    entityModel = itemModel
                    if (itemModel == EntityModel.BOMB){
                        playMode = Animation.PlayMode.NORMAL
                        frameDuration = DEFAULT_FRAME_DURATION * 1.5f
                        nextAnimation(AnimationType.ON)
                    }else{
                        nextAnimation(AnimationType.IDLE)
                    }
                }

                add<PhysicComponent>{
                   body = physicWorld.body(BodyDef.BodyType.DynamicBody){
                       userData = itemEntity
                       linearDamping = 1.5f
                       fixedRotation = true
                       position.set(bodyPos.x ,bodyPos.y )
                       box(0.7f,0.6f,if (itemModel == EntityModel.BOMB) Vector2(1.5f,1.26f) else Vector2(0.42f,0.35f)){
                           userData = itemEntity
                           isSensor = false
                           density = if (itemModel == EntityModel.BOMB) 70f else 30f
                           filter.categoryBits = if (itemModel == EntityModel.BOMB) BOMB_BIT else ITEM_BIT
                           filter.maskBits = KING_BIT or GROUND_BIT or BOX_BIT or BOMB_BIT or ROOF_BIT
                       }
                   }.apply {
                       applyLinearImpulse(Vector2((-70f..70f).random(),(40f..70f).random()),this.worldCenter,true)
                   }
                }
                add<ItemComponent>{
                    this.itemModel = itemModel
                    extraLife = if (itemModel == EntityModel.DIAMOND) 50f else if (itemModel == EntityModel.HEARTH) 30f else -50f
                }
                if (itemModel == EntityModel.BOMB){
                    add<AttackComponent>{
                        attackDamage = (20..30)
                        criticalHitChance = 0.6f
                    }
                }
            }
        }
    }
}

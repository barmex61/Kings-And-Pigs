package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.fatih.hoghavoc.component.AttackFixtureComponent
import com.fatih.hoghavoc.component.AttackType
import com.fatih.hoghavoc.utils.*
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.collections.map
import ktx.math.plus
import ktx.math.random
import ktx.math.times
import ktx.math.vec2
import kotlin.experimental.or
import kotlin.math.pow

@AllOf([AttackFixtureComponent::class])
class AttackFixtureSystem(
    private val attackFixtureComps : ComponentMapper<AttackFixtureComponent>,
    private val physicWorld: World,
    private val gameStage : Stage,
    private val textureAtlas: TextureAtlas
) : IteratingSystem() {

    private lateinit var attackFixtureComponent: AttackFixtureComponent
    private val animHashMap : HashMap<String,Animation<TextureRegionDrawable>> = hashMapOf()


    override fun onTickEntity(entity: Entity) {
        attackFixtureComponent = attackFixtureComps[entity]
        attackFixtureComponent.run {
            delay -= deltaTime
            if (hitPlayer) delay = 0f
            attackImage?.let {image->
                attackBody?.let { body->
                    image.setPosition(body.position.x - imageSize.x ,body.position.y - imageSize.y )
                    image.rotation = MathUtils.radiansToDegrees * body.angle
                }
            }

            if (delay <= 0f ){
                if (animation == null ){
                    animation = getAnimation(animationStr)
                }
                when(attackType){
                    AttackType.BOMB ->{
                        attackImage!!.run {
                            drawable = animation!!.getKeyFrame(stateTimer)
                            explodeBody?.let {
                               setPosition(it.position.x - imageSize.x + 0.1f ,it.position.y - imageSize.y - 0.3f )
                            }
                        }
                    }
                    AttackType.CANNON->{
                        attackImage!!.run {
                            drawable = animation!!.getKeyFrame(stateTimer)
                            explodeBody?.let {
                                setPosition(it.position.x - imageSize.x + 0.1f ,it.position.y - imageSize.y - 0.7f )
                            }
                        }
                    }
                    AttackType.BOX->{
                        boxPiecesHashMap!!.forEach { (flipImage, body) ->
                            body?.let {
                                flipImage.run {
                                    setPosition(body.position.x - width /2f,body.position.y - height/2f)
                                    rotation = MathUtils.radiansToDegrees * body.angle
                                }
                            }
                        }
                    }
                    else -> Unit
                }

                stateTimer += deltaTime
                when(animationStr){

                    "bomb_on"->{
                        if (animation!!.isAnimationFinished(stateTimer) && !destroyBodies){
                            animationStr = "bomb_explode"
                            stateTimer = 0f
                            animation = getAnimation(animationStr)

                        }
                    }

                    "bomb_explode"->{
                        if (animation!!.isAnimationFinished(stateTimer) && !destroyBodies){
                            gameStage.root.removeActor(attackImage)
                            destroyBodies = true
                        } else if (explodeBody == null && !destroyBodies){
                            val attackBodyPos : Vector2 = vec2()
                            attackBody?.let { attackBody->
                                attackBodyPos.set(attackBody.position)
                                physicWorld.destroyBody(attackBody)
                            }
                            attackBody = null
                            explodeBody = physicWorld.body {
                                userData = entity
                                position.set(attackBodyPos)
                                circle(1.5f, position = Vector2(0f,-0.3f)){
                                    density = 500f
                                    userData = BOMB_EXPLODE_FIXTURE
                                    filter.categoryBits = BOMB_BIT
                                    filter.maskBits = KING_BIT
                                    restitution = 10f
                                }
                                attackImage!!.setSize(3.2f,3.2f)
                                attackImage!!.setPosition(this.position.x - imageSize.x ,this.position.y - imageSize.y )
                            }
                        }
                    }

                    "box_pieces"->{
                        if (animation!!.isAnimationFinished(stateTimer) && !destroyBodies){
                            gameStage.root.removeActor(attackImage)
                            destroyBodies = true
                        }else if (!boxPiecesBodyCreated && !destroyBodies){
                            val attackBodyPos : Vector2 = vec2()
                            attackBody?.let { attackBody->
                                attackBodyPos.set(attackBody.position)
                                physicWorld.destroyBody(attackBody)
                            }
                            attackBody = null
                            gameStage.root.removeActor(attackImage)
                            boxPiecesBodyCreated = true
                            val posList = arrayOf(
                                Vector2(0.5f, 0.5f),
                                Vector2(-0.5f, 0.5f),
                                Vector2(-0.5f, -0.5f),
                                Vector2(0.5f, -0.5f)
                            )
                            boxPiecesHashMap!!.keys.forEachIndexed { index, flipImage ->
                                boxPiecesHashMap!![flipImage] = physicWorld.body(BodyDef.BodyType.DynamicBody){
                                        linearDamping = 2f
                                        fixedRotation = false
                                        position.set(attackBodyPos + posList[index])
                                        box(0.4f,0.4f){
                                            userData = BOX_PIECES_FIXTURES
                                            filter.categoryBits = BOX_BIT
                                            filter.maskBits = KING_BIT or GROUND_BIT
                                        }
                                    }.apply {
                                        applyLinearImpulse(posList[index] * (5f..10f).random(),worldCenter  ,true)
                                   }
                                flipImage.run {
                                    setPosition(boxPiecesHashMap!![flipImage]!!.position.x - width /2f,
                                        boxPiecesHashMap!![flipImage]!!.position.y - height/2f)
                                }

                                gameStage.addActor(flipImage)
                            }
                        }
                    }
                }
                if (destroyBodies){
                    explodeBody?.run {
                        physicWorld.destroyBody(explodeBody)
                    }
                    explodeBody = null
                    attackBody?.run {
                        physicWorld.destroyBody(attackBody)
                    }
                    attackBody = null
                    if (boxPiecesHashMap != null){
                        boxPiecesHashMap!!.forEach { (flipImage,body) ->
                            body?.run {
                                physicWorld.destroyBody(body)
                            }
                            gameStage.root.removeActor(flipImage)
                            boxPiecesHashMap!![flipImage] = null
                        }
                        boxPiecesHashMap = null
                    }
                    attackImage = null
                    destroyBodies = false
                    configureEntity(entity){
                        attackFixtureComps.remove(entity)
                    }
                    if (destroyEntity) {
                        world.remove(entity)
                    }
                }
            }
        }
    }

    private fun getAnimation(animationStr: String) : Animation<TextureRegionDrawable> = animHashMap.getOrPut(animationStr){
        val frames = textureAtlas.findRegions(animationStr)
        Animation(0.1f,frames.map { TextureRegionDrawable(it) },PlayMode.NORMAL)
    }

    companion object{
        const val BOMB_EXPLODE_FIXTURE = "BombExplodeFixture"
        const val BOX_PIECES_FIXTURES = "BoxPiecesFixtures"
    }

}

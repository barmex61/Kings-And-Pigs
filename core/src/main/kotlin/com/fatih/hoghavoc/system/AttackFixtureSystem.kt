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
            if (delay <= 0f || hitPlayer){
                if (animation == null){
                    animation = getAnimation(animationStr)
                }
                if (attackType != AttackType.BOX){
                    attackImage!!.run {
                        drawable = animation!!.getKeyFrame(stateTimer)
                    }
                }else{
                    boxPiecesHashMap!!.forEach { (flipImage, body) ->
                        body?.let {
                            flipImage.run {
                                setPosition(body.position.x - width /2f,body.position.y - height/2f)
                                rotation = MathUtils.radiansToDegrees * body.angle
                            }
                        }
                    }
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
                            destroyBodies = true
                            gameStage.root.removeActor(attackImage)
                        } else if (stateTimer > 0.15f && explodeBody == null && !destroyBodies){
                            explodeBody = physicWorld.body {
                                position.set(attackBody!!.position)
                                userData = entity
                                circle(1.5f, position = Vector2(0f,-0.3f)){
                                    density = 500f
                                    userData = BOMB_EXPLODE_FIXTURE
                                    filter.categoryBits = BOMB_BIT
                                    filter.maskBits = KING_BIT
                                    restitution = 10f
                                }
                            }

                        }
                    }

                    "box_pieces"->{
                        if (animation!!.isAnimationFinished(stateTimer) && !destroyBodies){
                            destroyBodies = true
                        }else if (!boxPiecesBodyCreated && !destroyBodies){
                            destroyBody(attackBody)
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
                                        position.set(attackBody!!.position + posList[index])
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
                    destroyBody(explodeBody)
                    destroyBody(attackBody)
                    if (boxPiecesHashMap != null){
                        boxPiecesHashMap!!.forEach { (flipImage,body) ->
                            destroyBody(body)
                            gameStage.root.removeActor(flipImage)
                            boxPiecesHashMap!![flipImage] = null
                        }
                        boxPiecesHashMap = null
                    }
                    attackBody = null
                    attackImage = null
                    destroyBodies = false
                    configureEntity(entity){
                        attackFixtureComps.remove(entity)
                    }

                }
            }
        }
    }

    private fun destroyBody(body:Body?){
        body?.fixtureList?.forEach {
            it.filterData.categoryBits = NOTHING_BIT
            body.destroyFixture(it)
            physicWorld.destroyBody(body)
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

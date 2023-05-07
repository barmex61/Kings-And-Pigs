package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.fatih.hoghavoc.actors.FlipImage
import com.fatih.hoghavoc.component.AttackFixtureComponent
import com.fatih.hoghavoc.component.AttackType
import com.fatih.hoghavoc.utils.BOX_BIT
import com.fatih.hoghavoc.utils.GROUND_BIT
import com.fatih.hoghavoc.utils.KING_BIT
import com.fatih.hoghavoc.utils.NOTHING_BIT
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
) : IteratingSystem(){

    private lateinit var attackFixtureComponent: AttackFixtureComponent
    private val animHashMap : HashMap<String,Animation<TextureRegionDrawable>> = hashMapOf()

    override fun onTickEntity(entity: Entity) {
        attackFixtureComponent = attackFixtureComps[entity]
        attackFixtureComponent.run {
            delay -= deltaTime
            println(physicWorld.bodyCount)
            if (delay> 0f){
                attackImage?.let {image->
                    attackBody?.let { body->
                        image.setPosition(body.position.x - imagePos.x ,body.position.y - imagePos.y )
                        image.rotation = MathUtils.radiansToDegrees * body.angle
                    }
                }
            }
            if (delay <= 0f){
                if (animation == null){
                    animation = getAnimation(animationStr)
                }
                if (attackType != AttackType.BOX){
                    attackImage!!.run {
                        drawable = animation!!.getKeyFrame(stateTimer)
                        setPosition(attackBody!!.position.x - imagePos.x,attackBody!!.position.y - imagePos.y)
                    }
                }else{
                    boxPieces?.forEachIndexed { index, flipImage ->
                        boxPiecesBody?.let { boxPiecesBody->
                            flipImage.run {
                                setPosition(boxPiecesBody[index].position.x - width /2f,boxPiecesBody[index].position.y - height/2f)
                            }
                        }
                    }
                }

                stateTimer += deltaTime
                when(animationStr){

                    "bomb_on"->{
                        if (animation!!.isAnimationFinished(stateTimer)){
                            animationStr = "bomb_explode"
                            stateTimer = 0f
                            animation = getAnimation(animationStr)
                        }
                    }

                    "bomb_explode"->{
                        if (animation!!.isAnimationFinished(stateTimer)){
                            destroyBodies = true
                            destroyBody(attackBody)
                            gameStage.root.removeActor(attackImage)
                        } else if (stateTimer > 0.15f && explodeBody == null){
                            explodeBody = physicWorld.body {
                                position.set(attackBody!!.position)
                                circle(1.5f, position = Vector2(0f,-0.3f)){
                                    density = 500f
                                    userData = BOMB_EXPLODE_FIXTURE
                                    filter.maskBits = KING_BIT
                                    restitution = 10f
                                }
                            }

                        }
                    }

                    "box_pieces"->{
                        if (animation!!.isAnimationFinished(stateTimer)){
                            destroyBodies = true
                        }else if (boxPiecesBody == null){
                            val posList = arrayOf(
                                Vector2(0.5f, 0.5f),
                                Vector2(-0.5f, 0.5f),
                                Vector2(-0.5f, -0.5f),
                                Vector2(0.5f, -0.5f)
                            )
                            boxPiecesBody = mutableListOf()
                            boxPieces!!.forEachIndexed { index, flipImage ->
                                boxPiecesBody!!.add(index,
                                    physicWorld.body(BodyDef.BodyType.DynamicBody){
                                        linearDamping = 2f
                                        position.set(attackBody!!.position + posList[index])
                                        box(0.4f,0.4f){
                                            userData = BOX_PIECES_FIXTURES
                                            filter.categoryBits = BOX_BIT
                                            filter.maskBits = KING_BIT or GROUND_BIT
                                        }
                                    }.apply {
                                        applyLinearImpulse(posList[index] * (0f..5f).random(),this.worldCenter,true)
                                    }
                                )

                                flipImage.run {
                                    setPosition(boxPiecesBody!![index].position.x - width /2f,boxPiecesBody!![index].position.y - height/2f)
                                }
                                gameStage.addActor(flipImage)
                            }
                            destroyBody(attackBody)
                            gameStage.root.removeActor(attackImage)

                        }
                    }
                }

                if (destroyBodies){
                    destroyBody(explodeBody)
                    destroyBody(attackBody)
                    if (boxPieces != null){
                        boxPieces!!.forEach {
                            gameStage.root.removeActor(it)
                        }
                        boxPieces = null
                    }
                    if (boxPiecesBody != null){
                        boxPiecesBody!!.forEach {body->
                            destroyBody(body)
                        }
                    }
                    attackBody = null
                    configureEntity(entity){
                        attackFixtureComps.remove(entity)
                    }
                    attackImage = null
                    destroyBodies = false
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
        Animation(0.25f,frames.map { TextureRegionDrawable(it) },PlayMode.NORMAL)
    }

    companion object{
        const val BOMB_EXPLODE_FIXTURE = "BombExplodeFixture"
        const val BOX_PIECES_FIXTURES = "BoxPiecesFixtures"
    }

}

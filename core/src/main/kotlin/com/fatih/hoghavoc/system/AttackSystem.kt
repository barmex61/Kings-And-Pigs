package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.fatih.hoghavoc.actors.FlipImage
import com.fatih.hoghavoc.utils.*
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.AttackEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.chain
import ktx.math.plus
import ktx.math.random
import ktx.math.times
import kotlin.experimental.or

@AllOf([AttackComponent::class])
class AttackSystem(
    private val attackComps : ComponentMapper<AttackComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val animComps : ComponentMapper<AnimationComponent>,
    private val stateComps : ComponentMapper<PlayerStateComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val attackFixtureComps : ComponentMapper<AttackFixtureComponent>,
    private val textureAtlas : TextureAtlas,
    private val gameStage : Stage,
    private val physicWorld : World

    ) : IteratingSystem() {

    private lateinit var attackComponent: AttackComponent
    private lateinit var physicComponent: PhysicComponent
    private lateinit var animationComponent: AnimationComponent
    private lateinit var imageComponent: ImageComponent
    private var playerBodyPos : Vector2? = null
    private var stateComponent: PlayerStateComponent? = null
    private val chainShapeArray : FloatArray = floatArrayOf(0.5f,1f,1.5f,0.5f,2.3f,0.8f,2.7f,1.4f,2.85f,2.5f,2.5f,3f,0.5f,1f)
    private val chainShapeArrayFlipped : FloatArray =  floatArrayOf(1.5f,1f,0.5f,0.5f,-0.3f,0.8f,-0.7f,1.4f,-0.85f,2.5f,-0.5f,3f,1.5f,1f)
    private val enemyChainShape : FloatArray = floatArrayOf(0.1f,-0.1f,-0.1f,0.1f,-0.3f,0.3f,-0.4f,0.6f,-0.4f,0.8f,-0.1f,1.1f,0.1f,-0.1f)
    private val enemyChainShapeFlipped : FloatArray = floatArrayOf(1.3f,-0.1f,1.5f,0.1f,1.7f,0.3f,1.8f,0.6f,1.8f,0.8f,1.5f,1.1f,1.3f,-0.1f)

    override fun onTickEntity(entity: Entity) {
        attackComponent = attackComps[entity]
        animationComponent = animComps[entity]
        physicComponent = physicComps[entity]
        imageComponent = imageComps[entity]
        stateComponent = stateComps.getOrNull(entity)
        if (playerBodyPos == null && entity in playerComps){
            if (playerBodyPos == null){
                playerBodyPos = physicComponent.body.position
            }
        }
        attackComponent.run {

            if (isReady){
                delay = maxDelay
                return
            }

            if (isAttacking) {

                when(attackType){
                    AttackType.MELEE_ATTACK ->{
                        if (delay == maxDelay){
                          gameStage.fireEvent(AttackEvent(physicComponent.body.linearVelocity.y > 0.1f))
                        }
                        if (delay > 0f){
                            meleeAttack(attackOnEnemy,entity)
                        }
                    }
                    AttackType.BOX ->{
                        if (delay == maxDelay)
                            boxAttack(entity,maxDelay, attackType)
                    }
                    AttackType.BOMB->{
                        if (delay == maxDelay)
                            bombAttack(entity,maxDelay,attackType)
                    }
                    else -> Unit
                }
            }
            if (delay <= maxDelay / 3f){
                destroyAttackBody(meleeAttackBody)
                meleeAttackBody = null
            }
            if (delay <= 0f){
                attackState = AttackState.READY
            }
            delay -= deltaTime
        }
    }



    private fun destroyAttackBody(body: Body?){
        body?.fixtureList?.firstOrNull()?.let {
            it.filterData.categoryBits = NOTHING_BIT
            body.destroyFixture(it)
            physicWorld.destroyBody(body)
        }
    }

    private fun boxAttack(entity: Entity,maxDelay:Float,attackType: AttackType){
        configureEntity(entity){
            if (entity !in attackFixtureComps){
                attackFixtureComps.add(entity){
                    if (boxPieces == null){
                        boxPieces = mutableListOf()
                        textureAtlas.findRegions("box_pieces").forEach {
                            boxPieces!!.add(
                                FlipImage(it).apply {
                                    setSize(0.4f,0.4f)
                                    setOrigin(Align.center)
                                }
                            )
                        }
                    }
                    delay = maxDelay
                    this.maxDelay = delay
                    this.attackType = attackType
                    imagePos.set(0.5f,0.5f)
                    attackBody =  physicWorld.body(BodyDef.BodyType.DynamicBody){
                        userData = entity
                        linearDamping = 1.5f
                        val myPosition =  if (!imageComponent.image.flipX){
                            Vector2(physicComponent.body.position.x - 0.4f,physicComponent.body.position.y +0.4f)
                        }else{
                            Vector2(physicComponent.body.position.x + 1.3f,physicComponent.body.position.y +0.4f)
                        }
                        position.set(myPosition.x,myPosition.y)
                        box{
                            density = 100f
                            userData = PIG_BOX_FIXTURE
                            filter.categoryBits = BOX_BIT
                            filter.maskBits = GROUND_BIT or KING_BIT
                        }
                    }
                    attackImage =  FlipImage(textureAtlas.findRegion("box_idle"),false).apply{
                        setSize(1f,1f)
                        setOrigin(Align.center)
                        setPosition(attackBody!!.position.x - 0.5f,attackBody!!.position.y - 0.5f)
                    }
                    val diffX = playerBodyPos!!.x - physicComponent.body.position.x
                    val diffY = playerBodyPos!!.y - physicComponent.body.position.y
                    attackBody!!.applyLinearImpulse(Vector2(diffX*200f,diffY*600f),attackBody!!.worldCenter + (0f..1f).random(),true)
                }
            }
        }
    }

    private fun bombAttack(entity: Entity,maxDelay : Float,attackType : AttackType){
        configureEntity(entity){
            if (entity !in attackFixtureComps){
                attackFixtureComps.add(entity){
                    delay = maxDelay
                    this.maxDelay = delay
                    this.attackType = attackType
                    imagePos.set(1.625f,1.5f)
                    attackBody =  physicWorld.body(BodyDef.BodyType.DynamicBody){
                        userData = entity
                        linearDamping = 1f
                        fixedRotation = true
                        val myPosition =  if (!imageComponent.image.flipX){
                            Vector2(physicComponent.body.position.x - 0.4f,physicComponent.body.position.y +0.4f)
                        }else{
                            Vector2(physicComponent.body.position.x + 1.5f,physicComponent.body.position.y +0.6f)
                        }
                        position.set(myPosition.x,myPosition.y)
                        box(0.8f,0.8f){
                            density = 50f
                            userData = PIG_BOMB_FIXTURE
                            filter.categoryBits = BOMB_BIT
                            filter.maskBits = GROUND_BIT or KING_BIT
                        }
                    }
                    attackImage =  FlipImage(textureAtlas.findRegion("bomb_idle"),false).apply{
                        setPosition(attackBody!!.position.x - imagePos.x,attackBody!!.position.y - imagePos.y)
                        setSize(3.25f,3.5f)
                    }
                    val diffX = playerBodyPos!!.x - physicComponent.body.position.x
                    val diffY = playerBodyPos!!.y - physicComponent.body.position.y
                    attackBody!!.applyLinearImpulse(Vector2(diffX*50f,diffY*100f),attackBody!!.worldCenter  ,true)
                }
            }
        }
    }

    private fun meleeAttack(attackOnEnemy : Boolean , entity: Entity){
        if (attackComponent.meleeAttackBody == null){
            attackComponent.meleeAttackBody = physicWorld.body(BodyDef.BodyType.StaticBody){
                position.set(physicComponent.body.position)
                userData = entity
                gravityScale = 0f
                chain(
                    if (!imageComponent.image.flipX && attackOnEnemy){
                        chainShapeArray
                    }else if (imageComponent.image.flipX && attackOnEnemy){
                        chainShapeArrayFlipped
                    }else if (!imageComponent.image.flipX && !attackOnEnemy){
                        enemyChainShape
                    }else{
                        enemyChainShapeFlipped
                    }
                ) {
                    isSensor = true
                    userData = entity
                    filter.categoryBits = if (attackOnEnemy) AXE_BIT else ENEMY_AXE_BIT
                    filter.maskBits = if (attackOnEnemy) PIG_BIT or KING_PIG_BIT else KING_BIT
                }
            }
        }else if (attackComponent.attackOnEnemy){
            attackComponent.meleeAttackBody!!.fixtureList.firstOrNull()?.run {
                filterData!!.categoryBits = NOTHING_BIT
                attackComponent.meleeAttackBody!!.destroyFixture(this)
                physicWorld.destroyBody(attackComponent.meleeAttackBody!!)
                attackComponent.meleeAttackBody = null
            }
        }
    }

    companion object{
        const val PIG_BOX_FIXTURE = "PigBoxFixture"
        const val PIG_BOMB_FIXTURE = "PigBombFixture"
    }
}




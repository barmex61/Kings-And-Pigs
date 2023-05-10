package com.fatih.hoghavoc.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.TimeUtils
import com.fatih.hoghavoc.utils.*
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.EnemyHitPlayerEvent
import com.fatih.hoghavoc.events.PlayerHitEnemyEvent
import com.fatih.hoghavoc.system.AttackFixtureSystem.Companion.BOMB_EXPLODE_FIXTURE
import com.fatih.hoghavoc.system.AttackSystem.Companion.PIG_BOMB_FIXTURE
import com.fatih.hoghavoc.system.AttackSystem.Companion.PIG_BOX_FIXTURE
import com.github.quillraven.fleks.*
import javax.swing.Box
import kotlin.experimental.or


@AllOf([PhysicComponent::class,ImageComponent::class])
class PhysicSystem(
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val physicWorld : com.badlogic.gdx.physics.box2d.World,
    private val moveComps : ComponentMapper<MoveComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val aiComps : ComponentMapper<AiComponent>,
    private val enemyComps : ComponentMapper<EnemyComponent>,
    private val attackFixtureComps : ComponentMapper<AttackFixtureComponent>,
    private val gameStage : Stage
) : IteratingSystem(interval = Fixed(1/144f)) , ContactListener{

    private lateinit var physicComponent: PhysicComponent
    private lateinit var imageComponent: ImageComponent
    private var kingEntity : Entity? = null
    private var pigEntity : Entity? = null
    private var collisionPigEntity : Entity? = null
    private var kingPigEntity : Entity? = null
    private var axeBody : Body? = null
    private var impulseTimer : Long = TimeUtils.millis()
    private var bombTimer : Long = TimeUtils.millis()
    private lateinit var fixture: Fixture


    init {
        physicWorld.setContactListener(this)
    }

    override fun onUpdate() {
        if (physicWorld.autoClearForces){
            physicWorld.autoClearForces = false
        }
        super.onUpdate()
        physicWorld.clearForces()
    }

    override fun onTick() {
        super.onTick()
        physicWorld.step(deltaTime*2f,6,2)
    }

    override fun onTickEntity(entity: Entity) {
        physicComponent = physicComps[entity]
        physicComponent.previousPosition.set(physicComponent.body.position)
        if (!physicComponent.impulse.isZero){
            physicComponent.body.applyLinearImpulse(physicComponent.impulse,physicComponent.body.worldCenter,true)
            physicComponent.impulse.setZero()
        }
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        imageComponent = imageComps[entity]
        physicComponent = physicComps[entity]
        physicComponent.run {
            imageOffset.set(
                if ( imageComponent.image.flipX)  physicComponent.size.x * 0.13f else physicComponent.size.x * 0.5f,
                0f
            )
            playerComps.getOrNull(entity)?.let {
                imageOffset.set(
                    if ( imageComponent.image.flipX)  physicComponent.size.x * 1.4f else physicComponent.size.x * 0.5f,
                    0f
                )
            }
            enemyComps.getOrNull(entity)?.let {
                when (it.entityModel) {
                    EntityModel.PIG -> {
                        imageOffset.set(
                            if ( imageComponent.image.flipX)  physicComponent.size.x * 0.13f else physicComponent.size.x * 0.5f,
                            0f
                        )
                    }
                    EntityModel.KING_PIG -> {
                        imageOffset.set(
                            if ( imageComponent.image.flipX)  physicComponent.size.x * 0.4f else physicComponent.size.x * 0.5f,
                            -0.1f
                        )
                    }
                    EntityModel.PIG_BOMB ->{
                        imageOffset.set(
                            if ( imageComponent.image.flipX)  physicComponent.size.x * 0.8f else physicComponent.size.x * 0.5f,
                            0f
                        )
                    }
                    else -> Unit
                }
            }
        }
        imageComponent.image.run {
            setPosition(
            MathUtils.lerp(physicComponent.previousPosition.x,physicComponent.body.position.x,alpha) - physicComponent.imageOffset.x ,
            MathUtils.lerp(physicComponent.previousPosition.y,physicComponent.body.position.y,alpha) - physicComponent.size.y * 0.5f * physicComponent.imageScaleDirection
            )
        }
    }

    private fun getEntity(fixtureA: Fixture,fixtureB:Fixture,categoryBit : Short,maskBit: Short?) : Entity {
        fixture = if (fixtureA.filterData.categoryBits == categoryBit) fixtureA else fixtureB
        maskBit?.let {
            fixture.body.fixtureList.find {
                it.shape is PolygonShape
            }!!.apply {
                filterData.maskBits = maskBit
            }
        }
        return fixture.userData as Entity
    }

    private fun setEntities(fixtureA: Fixture,fixtureB: Fixture,categoryBit: Short){
        if (categoryBit == COLLISION_DETECT_BIT){
            if (fixtureA.filterData.categoryBits == categoryBit){
                collisionPigEntity = fixtureA.userData as Entity
                kingEntity = fixtureB.userData as Entity
            }else{
                collisionPigEntity = fixtureB.userData as Entity
                kingEntity = fixtureA.userData as Entity
            }
            return
        }
        if (fixtureA.filterData.categoryBits == categoryBit){
            kingEntity = fixtureA.userData as Entity
            if (fixtureB.filterData.categoryBits == PIG_BIT) {
                pigEntity = fixtureB.userData as Entity
            }else{
                kingPigEntity = fixtureB.userData as Entity
            }
            axeBody = fixtureA.body
            return
        }
        kingEntity = fixtureB.userData as Entity
        if (fixtureB.filterData.categoryBits == PIG_BIT) {
            pigEntity = fixtureB.userData as Entity
        }else{
            kingPigEntity = fixtureB.userData as Entity
        }
        axeBody = fixtureB.body
    }


    override fun beginContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val fixtureB = contact.fixtureB

        val cDef = fixtureA.filterData.categoryBits or fixtureB.filterData.categoryBits
        when(cDef){
            BOX_BIT or KING_BIT -> {
                var boxEntity : Entity?= null
                if (fixtureA.filterData.categoryBits == KING_BIT && fixtureB.userData == PIG_BOX_FIXTURE){
                    kingEntity = fixtureA.userData as Entity
                    boxEntity = fixtureB.body.userData as Entity
                }else if (fixtureB.filterData.categoryBits == KING_BIT && fixtureA.userData == PIG_BOX_FIXTURE){
                    kingEntity = fixtureB.body.userData as Entity
                    boxEntity = fixtureA.body.userData as Entity
                }
                gameStage.run {
                    fireEvent(EnemyHitPlayerEvent(kingEntity!!,boxEntity))
                    boxEntity?.let {
                        attackFixtureComps.getOrNull(boxEntity)?.hitPlayer=true
                    }
                }
            }
            BOMB_BIT or KING_BIT -> {

                if (fixtureA.userData == PIG_BOMB_FIXTURE){
                    attackFixtureComps[fixtureA.body.userData as Entity].hitPlayer = true
                }

                if (fixtureB.userData == PIG_BOMB_FIXTURE ) {
                    attackFixtureComps[fixtureB.body.userData as Entity].hitPlayer = true
                }

                if (fixtureA.filterData.categoryBits == KING_BIT  && fixtureB.userData == BOMB_EXPLODE_FIXTURE && TimeUtils.millis() - bombTimer > 600L){
                    bombTimer = TimeUtils.millis()
                    kingEntity = fixtureA.userData as Entity
                    gameStage.fireEvent(EnemyHitPlayerEvent(kingEntity!!,fixtureB.body.userData as Entity))
                }
                if (fixtureB.filterData.categoryBits == KING_BIT && fixtureA.userData == BOMB_EXPLODE_FIXTURE && TimeUtils.millis() - bombTimer > 600L){
                    bombTimer = TimeUtils.millis()
                    kingEntity = fixtureB.userData as Entity
                    gameStage.fireEvent(EnemyHitPlayerEvent(kingEntity!!,fixtureA.body.userData as Entity))
                }

            }

            FOOT_BIT or GROUND_BIT ->{
                kingEntity = getEntity(fixtureA,fixtureB, FOOT_BIT,null)
                moveComps[kingEntity!!].canJump = true
            }
            ENEMY_FOOT_BIT or GROUND_BIT ->{
                pigEntity = getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT,null)
                moveComps[pigEntity!!].canJump = true
            }
            FOOT_BIT or ROOF_BIT ->{
                kingEntity = getEntity(fixtureA,fixtureB, FOOT_BIT, KING_FOOT_ENABLE_COLLISION)
                moveComps[kingEntity!!].canJump = true
            }
            ENEMY_FOOT_BIT or ROOF_BIT ->{
                pigEntity = getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT, PIG_FOOT_ENABLE_COLLISION)
                moveComps[pigEntity!!].canJump = true
            }
            ENEMY_FOOT_BIT or BOMB_BIT, ENEMY_FOOT_BIT or BOX_BIT, ENEMY_FOOT_BIT or KING_BIT, ENEMY_FOOT_BIT or PIG_BIT , ENEMY_FOOT_BIT or KING_PIG_BIT ->{
                if (fixtureA.filterData.categoryBits == ENEMY_FOOT_BIT){
                    moveComps[fixtureA.userData as Entity].canJump = true
                }else{
                    moveComps[fixtureB.userData as Entity].canJump = true
                }
            }
            FOOT_BIT or BOMB_BIT, FOOT_BIT or BOX_BIT, FOOT_BIT or KING_BIT, FOOT_BIT or PIG_BIT ,FOOT_BIT or KING_PIG_BIT ->{
                if (fixtureA.filterData.categoryBits == FOOT_BIT){
                    moveComps[fixtureA.userData as Entity].canJump = true
                }else{
                    moveComps[fixtureB.userData as Entity].canJump = true
                }
            }
            AXE_BIT or PIG_BIT, AXE_BIT or KING_PIG_BIT -> {
                setEntities(fixtureA, fixtureB, AXE_BIT)
                val isKingPig = AXE_BIT or KING_PIG_BIT == (fixtureA.filterData.categoryBits or fixtureB.filterData.categoryBits)
                val enemyEntity = if (isKingPig) kingPigEntity else pigEntity
                if (TimeUtils.millis() - impulseTimer >= TIME_BETWEEN_ATTACKS) {
                    physicComps[enemyEntity!!].run {
                        impulse.set(
                            (body.position.x - axeBody!!.position.x) * 1.5f,
                            body.position.y - axeBody!!.position.y
                        )
                    }
                    gameStage.fireEvent(PlayerHitEnemyEvent(kingEntity!!, enemyEntity))
                }
                impulseTimer = TimeUtils.millis()
            }
            COLLISION_DETECT_BIT or KING_BIT ->{
                setEntities(fixtureA,fixtureB, COLLISION_DETECT_BIT)
                playerComps[kingEntity!!].nearbyEntities.add(collisionPigEntity!!)
                aiComps[collisionPigEntity!!].nearbyEntities.add(kingEntity!!)
            }

            ENEMY_AXE_BIT or KING_BIT -> {
                val enemyEntity : Entity
                kingEntity = if (fixtureA.filterData.categoryBits == KING_BIT){
                    enemyEntity = fixtureB.body.userData as Entity
                    fixtureA.body.userData as Entity
                }else{
                    enemyEntity = fixtureA.body.userData as Entity
                    fixtureB.body.userData as Entity
                }
                gameStage.fireEvent(EnemyHitPlayerEvent(kingEntity!!,enemyEntity))
            }
        }
    }

    override fun endContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val fixtureB = contact.fixtureB
        val cDef = fixtureA.filterData.categoryBits or fixtureB.filterData.categoryBits
        when(cDef){
            FOOT_BIT or GROUND_BIT ->{
                kingEntity = getEntity(fixtureA,fixtureB, FOOT_BIT,null)
                moveComps[kingEntity!!].canJump = false
            }
            ENEMY_FOOT_BIT or GROUND_BIT ->{
                pigEntity = getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT,null)
                moveComps[pigEntity!!].canJump = false
            }
            FOOT_BIT or ROOF_BIT ->{
                kingEntity = getEntity(fixtureA,fixtureB, FOOT_BIT, KING_FOOT_DISABLE_COLLISION)
                moveComps[kingEntity!!].canJump = false
            }
            ENEMY_FOOT_BIT or ROOF_BIT ->{
                pigEntity = getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT, PIG_FOOT_DISABLE_COLLISION)
                moveComps[pigEntity!!].canJump = false
            }
            COLLISION_DETECT_BIT or KING_BIT ->{
                setEntities(fixtureA,fixtureB, COLLISION_DETECT_BIT)
                playerComps[kingEntity!!].nearbyEntities.remove(collisionPigEntity!!)
                aiComps[collisionPigEntity!!].nearbyEntities.remove(kingEntity!!)
            }
            ENEMY_FOOT_BIT or BOMB_BIT, ENEMY_FOOT_BIT or BOX_BIT, ENEMY_FOOT_BIT or KING_BIT, ENEMY_FOOT_BIT or PIG_BIT , ENEMY_FOOT_BIT or KING_PIG_BIT ->{
                if (fixtureA.filterData.categoryBits == ENEMY_FOOT_BIT){
                    moveComps[fixtureA.userData as Entity].canJump = false
                }else{
                    moveComps[fixtureB.userData as Entity].canJump = false
                }
            }
            FOOT_BIT or BOMB_BIT, FOOT_BIT or BOX_BIT, FOOT_BIT or KING_BIT, FOOT_BIT or PIG_BIT ,FOOT_BIT or KING_PIG_BIT ->{
                if (fixtureA.filterData.categoryBits == FOOT_BIT){
                    moveComps[fixtureA.userData as Entity].canJump = false
                }else{
                    moveComps[fixtureB.userData as Entity].canJump = false
                }
            }
        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold?)  = Unit

    override fun postSolve(contact: Contact, impulse: ContactImpulse?) = Unit
}

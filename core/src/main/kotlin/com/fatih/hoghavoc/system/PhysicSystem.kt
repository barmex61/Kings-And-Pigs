package com.fatih.hoghavoc.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
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
import com.fatih.hoghavoc.events.*
import com.fatih.hoghavoc.system.AttackFixtureSystem.Companion.BOMB_EXPLODE_FIXTURE
import com.fatih.hoghavoc.system.AttackSystem.Companion.CANNON_BALL_FIXTURE
import com.fatih.hoghavoc.system.AttackSystem.Companion.PIG_BOMB_FIXTURE
import com.fatih.hoghavoc.system.AttackSystem.Companion.PIG_BOX_FIXTURE
import com.github.quillraven.fleks.*
import kotlin.experimental.or


@AllOf([PhysicComponent::class,ImageComponent::class])
class PhysicSystem(
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val physicWorld : com.badlogic.gdx.physics.box2d.World,
    private val moveComps : ComponentMapper<MoveComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val aiComps : ComponentMapper<AiComponent>,
    private val attackFixtureComps : ComponentMapper<AttackFixtureComponent>,
    private val gameStage : Stage
) : IteratingSystem(interval = Fixed(1/300f)) , ContactListener{

    private lateinit var physicComponent: PhysicComponent
    private lateinit var imageComponent: ImageComponent
    private var playerHitTimer : Long = 0L
    private var bombTimer : Long = 0L
    private var enemyHitTimer : Long = 0L
    var destroyWorld = false
    var mapPath = "mapObjects/map1.tmx"
    var waitAnimation = false


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
        physicWorld.step(deltaTime * 2f,6,2)
        if (destroyWorld){
            gameStage.fireEvent(DestroyWorld())
            destroyWorld = false
        }
    }

    override fun onTickEntity(entity: Entity) {
        physicComponent = physicComps[entity]

        physicComponent.previousPosition.set(physicComponent.body.position)
        if (waitAnimation && entity in playerComps && !moveComps[entity].moveIn ){
            gameStage.fireEvent(CurrentMapChangeEvent(mapPath))
            waitAnimation = false
        }
        if (!physicComponent.impulse.isZero){
            physicComponent.body.applyLinearImpulse(physicComponent.impulse,physicComponent.body.worldCenter,true)
            physicComponent.impulse.setZero()
        }
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        imageComponent = imageComps[entity]
        physicComponent = physicComps[entity]
        imageComponent.image.run {
            setPosition(
            MathUtils.lerp(physicComponent.previousPosition.x,physicComponent.body.position.x,alpha) - if (flipX) physicComponent.flipImageOffset.x else physicComponent.imageOffset.x ,
            MathUtils.lerp(physicComponent.previousPosition.y,physicComponent.body.position.y,alpha) - physicComponent.size.y * 0.5f * physicComponent.imageScaleDirection
            )
        }
    }

    private fun getEntity(fixtureA: Fixture,fixtureB:Fixture,categoryBit : Short) : Entity {
        val fixture = if (fixtureA.filterData.categoryBits == categoryBit) fixtureA else fixtureB
        return fixture.userData as Entity
    }


    override fun beginContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val fixtureB = contact.fixtureB

        val cDef = fixtureA.filterData.categoryBits or fixtureB.filterData.categoryBits
        when(cDef){
            BOX_BIT or KING_BIT -> {
                val boxEntity : Entity?
                val kingEntity : Entity?
                if (fixtureA.filterData.categoryBits == KING_BIT && fixtureB.userData == PIG_BOX_FIXTURE){
                    kingEntity = fixtureA.userData as Entity
                    boxEntity = fixtureB.body.userData as Entity
                    gameStage.run {
                        fireEvent(EnemyHitPlayerEvent(kingEntity,boxEntity,null))
                        boxEntity.let {
                            attackFixtureComps.getOrNull(boxEntity)?.hitPlayer=true
                        }
                    }
                }else if (fixtureB.filterData.categoryBits == KING_BIT && fixtureA.userData == PIG_BOX_FIXTURE){
                    kingEntity = fixtureB.body.userData as Entity
                    boxEntity = fixtureA.body.userData as Entity
                    gameStage.run {
                        fireEvent(EnemyHitPlayerEvent(kingEntity,boxEntity,null))
                        boxEntity.let {
                            attackFixtureComps.getOrNull(boxEntity)?.hitPlayer=true
                        }
                    }
                }

            }
            BOMB_BIT or KING_BIT -> {
                var kingEntity : Entity
                if (fixtureA.userData == PIG_BOMB_FIXTURE){
                    attackFixtureComps[fixtureA.body.userData as Entity].hitPlayer = true
                }

                if (fixtureB.userData == PIG_BOMB_FIXTURE ) {
                    attackFixtureComps[fixtureB.body.userData as Entity].hitPlayer = true
                }

                if (fixtureA.userData == CANNON_BALL_FIXTURE){
                    attackFixtureComps[fixtureA.body.userData as Entity].run {
                        hitPlayer = true
                    }
                }

                if (fixtureB.userData == CANNON_BALL_FIXTURE){
                    attackFixtureComps[fixtureB.body.userData as Entity].run {
                        hitPlayer = true
                    }
                }

                if (fixtureA.filterData.categoryBits == KING_BIT  && fixtureB.userData == BOMB_EXPLODE_FIXTURE && TimeUtils.millis() - bombTimer > 600L){
                    bombTimer = TimeUtils.millis()
                    kingEntity = fixtureA.userData as Entity
                    gameStage.fireEvent(EnemyHitPlayerEvent(kingEntity,fixtureB.body.userData as Entity,CANNON_EXPLOSION))
                }
                if (fixtureB.filterData.categoryBits == KING_BIT && fixtureA.userData == BOMB_EXPLODE_FIXTURE && TimeUtils.millis() - bombTimer > 600L){
                    bombTimer = TimeUtils.millis()
                    kingEntity = fixtureB.userData as Entity
                    gameStage.fireEvent(EnemyHitPlayerEvent(kingEntity,fixtureA.body.userData as Entity,
                        CANNON_EXPLOSION))
                }

            }

            FOOT_BIT or GROUND_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, FOOT_BIT)].canJump = true
            }
            ENEMY_FOOT_BIT or GROUND_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT)].canJump = true
            }
            FOOT_BIT or ROOF_BIT ->{

                moveComps[getEntity(fixtureA,fixtureB, FOOT_BIT).apply {
                    physicComps[this].body.fixtureList.find {
                        it.shape is PolygonShape
                    }!!.apply {
                        filterData.maskBits = KING_FOOT_ENABLE_COLLISION
                    }
                }].canJump = true
            }
            ENEMY_FOOT_BIT or ROOF_BIT ->{

                moveComps[getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT).apply {
                    physicComps[this].body.fixtureList.find {
                        it.shape is PolygonShape
                    }!!.apply {
                        filterData.maskBits = PIG_FOOT_ENABLE_COLLISION
                    }
                }].canJump = true
            }
            ENEMY_FOOT_BIT or BOMB_BIT, ENEMY_FOOT_BIT or BOX_BIT, ENEMY_FOOT_BIT or KING_BIT, ENEMY_FOOT_BIT or PIG_BIT , ENEMY_FOOT_BIT or KING_PIG_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT)].canJump = true
            }
            FOOT_BIT or BOMB_BIT, FOOT_BIT or BOX_BIT, FOOT_BIT or KING_BIT, FOOT_BIT or PIG_BIT ,FOOT_BIT or KING_PIG_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, FOOT_BIT)].canJump = true
            }
            AXE_BIT or PIG_BIT, AXE_BIT or KING_PIG_BIT -> {
                val kingEntity : Entity
                val enemyEntity : Entity
                val axeBodyPosition : Vector2
                if (fixtureA.filterData.categoryBits == AXE_BIT){
                    kingEntity = fixtureA.userData as Entity
                    enemyEntity = fixtureB.userData as Entity
                    axeBodyPosition = fixtureA.body.position
                }else{
                    kingEntity = fixtureB.userData as Entity
                    enemyEntity = fixtureA.userData as Entity
                    axeBodyPosition = fixtureB.body.position
                }
                if (TimeUtils.millis() - playerHitTimer >= TIME_BETWEEN_ATTACKS) {
                    physicComps[enemyEntity].run {
                        body.applyLinearImpulse(Vector2((body.position.x - axeBodyPosition.x) * 15f,
                            body.position.y - axeBodyPosition.y * 15f),body.worldCenter,true)
                    }
                    gameStage.fireEvent(PlayerHitEnemyEvent(kingEntity, enemyEntity))
                }
                playerHitTimer = TimeUtils.millis()
            }
            COLLISION_DETECT_BIT or KING_BIT ->{
                val kingEntity = getEntity(fixtureA,fixtureB, KING_BIT)
                val enemyEntity = getEntity(fixtureA,fixtureB, COLLISION_DETECT_BIT)
                playerComps[kingEntity].nearbyEntities.add(enemyEntity)
                aiComps[enemyEntity].nearbyEntities.add(kingEntity)
            }

            ENEMY_AXE_BIT or KING_BIT -> {
                val enemyEntity : Entity = getEntity(fixtureA,fixtureB, ENEMY_AXE_BIT)
                val kingEntity : Entity = getEntity(fixtureA,fixtureB, KING_BIT)
                if (TimeUtils.millis() - enemyHitTimer >= TIME_BETWEEN_ATTACKS)
                    gameStage.fireEvent(EnemyHitPlayerEvent(kingEntity,enemyEntity,null))
                enemyHitTimer = TimeUtils.millis()
            }

            KING_BIT or PORTAL_BIT ->{
                val kingEntity = getEntity(fixtureA,fixtureB, KING_BIT)
                val path = if (fixtureA.filterData.categoryBits == KING_BIT) fixtureB.userData as String else fixtureA.userData as String
                mapPath = path
                moveComps[kingEntity].run {
                     if (canJump && !moveIn){
                        if (world.family(allOf = arrayOf(EnemyComponent::class)).isEmpty){
                            root = true
                            moveIn = true
                            waitAnimation = true
                            gameStage.fireEvent(StartStageActionEvent())
                        }
                    }
                }
            }

        }
    }

    override fun endContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val fixtureB = contact.fixtureB
        val cDef = fixtureA.filterData.categoryBits or fixtureB.filterData.categoryBits
        when(cDef){
            FOOT_BIT or GROUND_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, FOOT_BIT)].canJump = false
            }
            ENEMY_FOOT_BIT or GROUND_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT)].canJump = false
            }
            FOOT_BIT or ROOF_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, FOOT_BIT).apply {
                    physicComps[this].body.fixtureList.find {
                        it.shape is PolygonShape
                    }!!.apply {
                        filterData.maskBits = KING_FOOT_DISABLE_COLLISION
                    }
                }].canJump = false
            }
            ENEMY_FOOT_BIT or ROOF_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT).apply {
                    physicComps[this].body.fixtureList.find {
                        it.shape is PolygonShape
                    }!!.apply {
                        filterData.maskBits = PIG_FOOT_DISABLE_COLLISION
                    }
                }].canJump = false
            }
            COLLISION_DETECT_BIT or KING_BIT ->{
                val enemyEntity  = getEntity(fixtureA,fixtureB, COLLISION_DETECT_BIT)
                val kingEntity = getEntity(fixtureA,fixtureB, KING_BIT)
                playerComps[kingEntity].nearbyEntities.remove(enemyEntity)
                aiComps[enemyEntity].nearbyEntities.remove(kingEntity)
            }
            ENEMY_FOOT_BIT or BOMB_BIT, ENEMY_FOOT_BIT or BOX_BIT, ENEMY_FOOT_BIT or KING_BIT, ENEMY_FOOT_BIT or PIG_BIT , ENEMY_FOOT_BIT or KING_PIG_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, ENEMY_FOOT_BIT)].canJump = false
            }
            FOOT_BIT or BOMB_BIT, FOOT_BIT or BOX_BIT, FOOT_BIT or KING_BIT, FOOT_BIT or PIG_BIT ,FOOT_BIT or KING_PIG_BIT ->{
                moveComps[getEntity(fixtureA,fixtureB, FOOT_BIT)].canJump = false
            }

        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold?) = Unit

    override fun postSolve(contact: Contact, impulse: ContactImpulse?) = Unit
}

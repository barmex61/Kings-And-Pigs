package com.fatih.hoghavoc.system

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.fatih.hoghavoc.utils.*
import com.fatih.hoghavoc.actors.FlipImage
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.MapChangeEvent
import com.fatih.hoghavoc.events.RespawnPlayer
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.app.gdxError
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.edge
import ktx.math.times
import ktx.tiled.layer
import ktx.tiled.x
import ktx.tiled.y
import kotlin.experimental.or

@AllOf([SpawnComponent::class])
class EntitySpawnSystem(
    private val spawnComps : ComponentMapper<SpawnComponent>,
    private val physicWorld : World
) : IteratingSystem() , EventListener{

    private lateinit var spawnComponent: SpawnComponent
    private val spawnCache = hashMapOf<Pair<String,Float>,SpawnConfig>()
    private lateinit var physicComponent: PhysicComponent

    private fun getSpawnConfig(spawnComp : SpawnComponent) : SpawnConfig = spawnCache.getOrPut(Pair(spawnComp.name,spawnComp.moveRange)){
        when(spawnComp.name){
            KING ->{
                SpawnConfig(
                    model = EntityModel.KING,
                    bodyType = BodyDef.BodyType.DynamicBody,
                    canAttack = true,
                    imageScaling = Vector2(1.8f,1.8f),
                    physicScaling = Vector2(0.22f,0.4f),
                    physicOffset = Vector2(1.05f,2f),
                    categoryBit = KING_BIT,
                    attackDelay = MELEE_ATTACK_DELAY,
                    maskBits = KING_PIG_BIT or CANNON_BIT or PIG_BIT or GROUND_BIT or BOX_BIT or COLLISION_DETECT_BIT or ENEMY_AXE_BIT or BOMB_BIT,
                    speedScaling = 0.5f,
                    lifeScaling = 1f,
                    maxLife = 5000f,
                    attackFixtureDestroyDelay = 0.4f,
                )
            }
            KING_PIG ->{
                SpawnConfig(
                    model = EntityModel.KING_PIG,
                    bodyType = BodyDef.BodyType.DynamicBody,
                    physicScaling = Vector2(0.45f,0.6f),
                    physicOffset = Vector2(0.60f,0f),
                    canAttack = true,
                    attackDelay = MELEE_ATTACK_DELAY,
                    categoryBit = KING_PIG_BIT,
                    maskBits = KING_BIT  or BOX_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or KING_PIG_BIT or BOMB_BIT or CANNON_BIT,
                    speedScaling = 0.3f,
                    lifeScaling = 0.8f,
                    aiTreePath = "ai/slime.tree",
                    maxLife = 400f,
                    moveRange = spawnComp.moveRange,
                    extraAttackRange = 0.9f,
                    attackFixtureDestroyDelay = 0.4f,
                    collisionRange = 3f
                )
            }
            PIG, PIG_FIRE, PIG_BOMB, PIG_BOX ->{
                val speedScaling: Float
                val entityModel: EntityModel
                val imageOffset : Vector2
                val physicOffset : Vector2
                val physicScaling : Vector2
                val maskBits : Short
                val extraAttackRange : Float
                val attackType : AttackType
                val attackDelay : Float
                val collisionRange :Float
                val attackFixtureDestroyDelay : Float
                when (spawnComp.name) {
                    PIG -> {
                        imageOffset = Vector2(1f,1f)
                        speedScaling = 0.8f
                        physicScaling = Vector2(0.43f,0.5f)
                        entityModel = EntityModel.PIG
                        physicOffset = Vector2(0.70f,0f)
                        extraAttackRange = 0.9f
                        collisionRange = 3f
                        attackFixtureDestroyDelay = 0.4f
                        attackDelay = MELEE_ATTACK_DELAY
                        attackType = AttackType.MELEE_ATTACK
                        maskBits = KING_BIT or BOX_BIT or BOMB_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or PIG_BIT or CANNON_BIT
                    }
                    PIG_FIRE -> {
                        imageOffset = Vector2(0.7f,0.65f)
                        entityModel = EntityModel.PIG_FIRE
                        speedScaling = 0.5f
                        physicOffset = Vector2(0.33f,0f)
                        physicScaling = Vector2(0.54f,0.77f)
                        extraAttackRange = 1f
                        attackFixtureDestroyDelay = 0.4f
                        collisionRange = 3f
                        attackDelay = MELEE_ATTACK_DELAY
                        attackType = AttackType.FIRE
                        maskBits = KING_BIT or BOX_BIT or BOX_BIT or BOMB_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or PIG_BIT or ROOF_BIT
                    }
                    PIG_BOMB -> {
                        imageOffset = Vector2(0.8f,0.85f)
                        entityModel = EntityModel.PIG_BOMB
                        physicScaling = Vector2(0.45f,0.55f)
                        speedScaling = 0.5f
                        attackDelay = BOMB_ATTACK_DELAY
                        extraAttackRange = 2.5f
                        collisionRange = 5f
                        attackFixtureDestroyDelay = 0.4f
                        attackType = AttackType.BOMB
                        physicOffset = Vector2(0.33f,0f)
                        maskBits = KING_BIT or BOX_BIT or BOMB_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or PIG_BIT or CANNON_BIT
                    }
                    else -> {
                        imageOffset = Vector2(0.8f,0.85f)
                        speedScaling = 0.6f
                        physicOffset = Vector2(0.57f,0f)
                        physicScaling = Vector2(0.45f,0.55f)
                        extraAttackRange = 4f
                        attackFixtureDestroyDelay = 5f
                        attackDelay = BOX_ATTACK_DELAY
                        collisionRange = 8f
                        attackType = AttackType.BOX
                        entityModel =  EntityModel.PIG_BOX.apply { identify = spawnComp.identify
                        maskBits = KING_BIT or BOX_BIT or BOMB_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or PIG_BIT or CANNON_BIT
                        }
                    }
                }
                SpawnConfig(
                    model = entityModel,
                    bodyType = BodyDef.BodyType.DynamicBody,
                    physicScaling = physicScaling,
                    physicOffset = physicOffset,
                    canAttack = true,
                    categoryBit = PIG_BIT,
                    maskBits = maskBits,
                    attackFixtureDestroyDelay = attackFixtureDestroyDelay,
                    attackType = attackType,
                    attackDelay = attackDelay,
                    speedScaling = speedScaling,
                    collisionRange = collisionRange,
                    lifeScaling = 0.5f,
                    aiTreePath = "ai/slime.tree",
                    maxLife = 20f,
                    moveRange = spawnComp.moveRange,
                    imageScaling = imageOffset,
                    extraAttackRange = extraAttackRange
                )
            }
            BOX ->{
                SpawnConfig(
                    model = EntityModel.BOX,
                    bodyType = BodyDef.BodyType.StaticBody,
                    canAttack = false,
                    imageScaling = Vector2(0.63f,0.63f),
                    physicScaling = Vector2(1f,1f),
                    physicOffset = Vector2(0f,0f),
                    categoryBit = BOX_BIT,
                    maskBits = GROUND_BIT or ROOF_BIT or BOMB_BIT or BOX_BIT or KING_BIT or KING_PIG_BIT or PIG_BIT or FOOT_BIT or ENEMY_FOOT_BIT or CANNON_BIT,
                    speedScaling = 0f,
                    lifeScaling = 0f,
                    moveRange = spawnComp.moveRange
                )
            }
            BOMB ->{
                SpawnConfig(
                    model = EntityModel.BOMB,
                    bodyType = BodyDef.BodyType.DynamicBody,
                    canAttack = false,
                    imageScaling = Vector2(1.5f,1.5f),
                    physicScaling = Vector2(0.23f,0.23f),
                    physicOffset = Vector2(1.19f,0.9f),
                    categoryBit = BOMB_BIT,
                    maskBits = GROUND_BIT or ROOF_BIT or BOX_BIT or KING_BIT or KING_PIG_BIT or PIG_BIT or FOOT_BIT or ENEMY_FOOT_BIT or BOMB_BIT or CANNON_BIT,
                    speedScaling = 0f,
                    lifeScaling = 0f,
                    moveRange = spawnComp.moveRange
                )
            }
            CANNON ->{
                SpawnConfig(
                    model = EntityModel.CANNON,
                    bodyType = BodyDef.BodyType.DynamicBody,
                    canAttack = false,
                    imageScaling = Vector2(1.57f,1f),
                    physicScaling = Vector2(0.55f,0.63f),
                    physicOffset = Vector2(1.2f,0.25f),
                    categoryBit = CANNON_BIT,
                    maskBits = GROUND_BIT or ROOF_BIT or BOMB_BIT or BOX_BIT or KING_BIT or KING_PIG_BIT or PIG_BIT or FOOT_BIT or ENEMY_FOOT_BIT or BOMB_BIT or CANNON_BIT,
                    speedScaling = 0f,
                    lifeScaling = 0f,
                    moveRange = spawnComp.moveRange
                )
            }

            else -> gdxError("There is no config for the name of ${spawnComp.name}")
        }
    }

    override fun onTickEntity(entity: Entity) {
        spawnComponent = spawnComps[entity]
        val spawnConfig = getSpawnConfig(spawnComponent)
        spawnComponent.run {
            world.entity { spawnEntity->
                val imageSize = Vector2(
                    DEFAULT_ENTITY_SIZE * spawnConfig.imageScaling.x,
                    DEFAULT_ENTITY_SIZE * spawnConfig.imageScaling.y)
                add<ImageComponent>{
                    image = FlipImage().apply {
                        setSize(imageSize.x,imageSize.y)
                        setPosition(location.x,location.y)
                    }
                }
                add<AnimationComponent>{
                    entityModel = spawnConfig.model
                    animType = when (spawnConfig.model) {
                        EntityModel.KING -> {
                            frameDuration = DEFAULT_FRAME_DURATION * 1.5f
                            AnimationType.DOOR_OUT
                        }
                        EntityModel.PIG_FIRE -> {
                            frameDuration = DEFAULT_FRAME_DURATION * 2f
                            AnimationType.PREPARE
                        }
                        EntityModel.PIG_BOX ->{
                            if (spawnConfig.model.identify == "LookOut"){
                                AnimationType.LOOKING_OUT
                            }else{
                                AnimationType.IDLE
                            }
                        }
                        else -> {
                            AnimationType.IDLE
                        }
                    }
                    if (spawnConfig.model!= EntityModel.CANNON && spawnConfig.model != EntityModel.BOMB){
                        nextAnimation(animType)
                    }else{
                        nextTexture(TextureType.IDLE)
                    }
                }
                physicComponent = add{
                    size.set(imageSize * spawnConfig.physicScaling)
                    offset.set(spawnConfig.physicOffset)
                    body = physicWorld.body(spawnConfig.bodyType){
                        fixedRotation = true
                        position.set(location.x + size.x * 0.5f ,location.y + size.y * 0.5f)
                        if (spawnConfig.model == EntityModel.BOMB || spawnConfig.model == EntityModel.CANNON) linearDamping = 1f
                        userData = spawnEntity
                        box(size.x,size.y,offset){
                            isSensor = false
                            filter.categoryBits = spawnConfig.categoryBit
                            filter.maskBits = spawnConfig.maskBits
                            friction = 0f
                            if (spawnConfig.model == EntityModel.BOMB) density = 150f
                            if (spawnConfig.model == EntityModel.CANNON) density = 200f
                            restitution = 0f
                            userData = spawnEntity
                        }
                        if (spawnConfig.model == EntityModel.PIG_BOX){
                            box(1.15f,1f,position = Vector2(0.55f,0.45f)){
                                filter.categoryBits = BOX_BIT
                                filter.maskBits = KING_BIT or GROUND_BIT or FOOT_BIT or ENEMY_FOOT_BIT
                                density = 100f
                                userData = BOX_ON_HEAD
                            }
                        }
                        if (spawnConfig.model == EntityModel.KING){
                            imageScaleDirection = -0.5f
                            edge(1.1f,1f,1.1f,1.5f){
                                isSensor = true
                                userData = spawnEntity
                                filter.categoryBits = FOOT_BIT
                                filter.maskBits = KING_FOOT_ENABLE_COLLISION
                            }

                        }else if(spawnConfig.speedScaling != 0f && spawnConfig.model != EntityModel.BOX && spawnConfig.model != EntityModel.CANNON && spawnConfig.model != EntityModel.BOMB){

                            val x = if (spawnConfig.model == EntityModel.PIG_BOX || spawnConfig.model == EntityModel.KING_PIG) 0.6f else  0.7f
                            val yVector = if (spawnConfig.model != EntityModel.KING_PIG && spawnConfig.model == EntityModel.PIG_BOX) Vector2(-0.5f,-0.3f) else Vector2(-0.7f,-0.4f)
                            edge(x,yVector.x,x,yVector.y){
                                isSensor = true
                                userData = spawnEntity
                                filter.categoryBits = ENEMY_FOOT_BIT
                                filter.maskBits = GROUND_BIT or BOX_BIT or ROOF_BIT
                            }
                        }
                    }
                }
                if (spawnConfig.canAttack){
                    add<AttackComponent>{
                        attackState = AttackState.READY
                        if (spawnConfig.model != EntityModel.KING)
                            attackOnEnemy = false
                        when(spawnConfig.model){
                            //SPAWNCONFIGE TAŞI PIGBOX YANLIS YERDE
                            EntityModel.KING_PIG->{attackDamage = (1..6)}
                            EntityModel.PIG->{attackDamage = (1..3)}
                            EntityModel.KING->{attackDamage = (1..7)}

                            else -> Unit
                        }
                        delay = spawnConfig.attackDelay
                        maxDelay = spawnConfig.attackDelay
                        attackRange = spawnConfig.extraAttackRange
                        attackType = spawnConfig.attackType
                    }
                }
                if (spawnConfig.speedScaling != 0f){
                    add<MoveComponent>{
                        speed.x = DEFAULT_SPEED * spawnConfig.speedScaling
                        if (spawnConfig.model != EntityModel.KING)
                            moveRange = spawnConfig.moveRange

                    }
                }
                if (spawnConfig.lifeScaling > 0f){
                    add<LifeComponent>{
                        maxLife = spawnConfig.maxLife
                        life = maxLife
                        regenerationSpeed = 0f
                        takeDamage = 0f
                    }
                }
                if (spawnConfig.bodyType != BodyDef.BodyType.StaticBody){
                    add<CollisionComponent>()
                }

                if (spawnConfig.model == EntityModel.KING){
                    add<PlayerComponent>()
                    add<PlayerStateComponent>()
                }else if (spawnConfig.model != EntityModel.BOX){
                    add<EnemyComponent>{
                        entityModel = spawnConfig.model
                    }
                }
                if (spawnConfig.aiTreePath.isNotBlank()){
                    add<AiComponent>{
                        treePath = spawnConfig.aiTreePath
                    }
                    physicComponent.body.circle(spawnConfig.collisionRange,Vector2(physicComponent.size.x * 0.7f,0f)){
                        isSensor = true
                        filter.categoryBits = COLLISION_DETECT_BIT
                        filter.maskBits = KING_BIT
                        userData = spawnEntity
                    }
                }
            }
        }

        world.remove(entity)
    }

    private fun createCollisionObjects(mapObject: MapObject){
        when(mapObject){
            is RectangleMapObject ->{
                world.entity {entity->
                    add<PhysicComponent>{
                        size.set(mapObject.rectangle.width * UNIT_SCALE , mapObject.rectangle.height * UNIT_SCALE)
                        body = physicWorld.body(BodyDef.BodyType.StaticBody){
                            position.set(
                                (mapObject.rectangle.x + mapObject.rectangle.width/2f) * UNIT_SCALE,
                                (mapObject.rectangle.y + mapObject.rectangle.height/2f) * UNIT_SCALE
                            )
                            userData = entity
                            box(size.x,size.y){
                                filter.categoryBits = if(mapObject.name == "Roof") ROOF_BIT else GROUND_BIT
                                restitution = 0f
                                friction = 0f
                                isSensor = false
                            }
                        }
                    }
                }
            }
        }
    }

    override fun handle(event: Event): Boolean {
        return when(event){
            is RespawnPlayer ->{
                world.entity {
                    add<SpawnComponent>{
                        name = "King"
                        location.set(
                            5f,
                            5f
                        )
                    }
                }
                true
            }
            is MapChangeEvent ->{
                val objectLayer = event.map.layer("EntityLayer")
                objectLayer.objects.forEach {mapObject->
                    val objName = mapObject.name ?: gdxError("There is no name implemented for object $mapObject")
                    val moveRange = mapObject.properties.get("moveRange")?.let {
                        it as Float
                    }
                    val identify = mapObject.properties.get("identify")?.let {
                        it as String
                    }

                    world.entity {
                        add<SpawnComponent>{
                            name = objName
                            this.identify = identify?: ""
                            this.moveRange = moveRange?:0f
                            location.set(
                                mapObject.x * UNIT_SCALE,
                                mapObject.y * UNIT_SCALE
                            )
                        }
                    }
                }


                val collisionLayer = event.map.layer("CollisionLayer")
                collisionLayer.objects.forEach { mapObject ->
                     createCollisionObjects(mapObject)
                }
                true
            }
            else -> false
        }
    }

    companion object{
        const val BOX_ON_HEAD = "BoxOnHead"
    }

}

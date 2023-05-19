package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
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
import ktx.tiled.*
import kotlin.coroutines.coroutineContext
import kotlin.experimental.or

@AllOf([SpawnComponent::class])
class EntitySpawnSystem(
    private val spawnComps : ComponentMapper<SpawnComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val physicWorld : World
) : IteratingSystem() , EventListener{

    private lateinit var spawnComponent: SpawnComponent
    private lateinit var physicComponent: PhysicComponent
    private var size = 0
    private var mapChange = false
    private var playerEntity : Entity? = null

    private fun getSpawnConfig(spawnComp : SpawnComponent) : SpawnConfig {
       return when(spawnComp.name){
            GATE ->{
                SpawnConfig(
                    model = EntityModel.DOOR,
                    bodyType = BodyType.StaticBody,
                )
            }
            KING ->{
                SpawnConfig(
                    model = EntityModel.KING,
                    bodyType = BodyType.DynamicBody,
                    canAttack = true,
                    critChance = 0.5f,
                    density = 100f,
                    imageScaling = Vector2(1.8f,1.8f),
                    physicScaling = Vector2(0.2f,0.4f),
                    physicOffset = Vector2(1.52f,2f),
                    categoryBit = KING_BIT,
                    attackDelay = MELEE_ATTACK_DELAY,
                    maskBits = ITEM_BIT or PORTAL_BIT or KING_PIG_BIT or CANNON_BIT or PIG_BIT or GROUND_BIT or BOX_BIT or COLLISION_DETECT_BIT or ENEMY_AXE_BIT or BOMB_BIT,
                    speedScaling = 0.5f,
                    lifeScaling = 1f,
                    attackDamage = (5..15),
                    maxLife = 100f,
                    attackFixtureDestroyDelay = 0.4f,
                )
            }
            KING_PIG ->{
                SpawnConfig(
                    model = EntityModel.KING_PIG,
                    bodyType = BodyType.DynamicBody,
                    physicScaling = Vector2(0.33f,0.6f),
                    physicOffset = Vector2(0.84f,0.05f),
                    canAttack = true,
                    density = 250f,
                    score = 500,
                    critChance = 0.7f,
                    attackDelay = MELEE_ATTACK_DELAY * 2f,
                    categoryBit = KING_PIG_BIT,
                    attackDamage = (7..10),
                    maskBits = KING_BIT  or BOX_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or KING_PIG_BIT or BOMB_BIT or CANNON_BIT,
                    speedScaling = 0.3f,
                    lifeScaling = 0.8f,
                    aiTreePath = "ai/pig.tree",
                    maxLife = 40f,
                    moveRange = spawnComp.moveRange,
                    extraAttackRange = 0.7f,
                    attackFixtureDestroyDelay = 0.4f,
                    collisionRange = 3f
                )
            }
            PIG_BOX_INSIDE ->{
                SpawnConfig(
                    model = EntityModel.PIG_BOX_INSIDE,
                    bodyType = BodyType.DynamicBody,
                    physicScaling = Vector2(0.7f,0.7f),
                    physicOffset = Vector2(0.3f,0f),
                    canAttack = true,
                    density = 250f,
                    score = 500,
                    critChance = 0.7f,
                    attackDelay = MELEE_ATTACK_DELAY * 2f,
                    categoryBit = PIG_BIT,
                    attackDamage = (10..15),
                    maskBits = KING_BIT  or BOX_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or KING_PIG_BIT or BOMB_BIT or CANNON_BIT,
                    speedScaling = 0.3f,
                    lifeScaling = 0.8f,
                    aiTreePath = "ai/pig.tree",
                    maxLife = 60f,
                    moveRange = spawnComp.moveRange,
                    extraAttackRange = 0.7f,
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
                val attackDamage : IntRange
                val collisionRange :Float
                val density : Float
                var bodyType = BodyType.DynamicBody
                val critChance : Float
                val score : Int
                val attackFixtureDestroyDelay : Float
                when (spawnComp.name) {
                    PIG -> {
                        imageOffset = Vector2(1f,1f)
                        speedScaling = 0.8f
                        physicScaling = Vector2(0.37f,0.5f)
                        entityModel = EntityModel.PIG
                        physicOffset = Vector2(0.84f,0f)
                        extraAttackRange = 0.7f
                        critChance = 0.2f
                        density = 230f
                        score = 150
                        collisionRange = 3f
                        attackDamage = (3..6)
                        attackFixtureDestroyDelay = 0.4f
                        attackDelay = MELEE_ATTACK_DELAY * 2f
                        attackType = AttackType.MELEE_ATTACK
                        maskBits = KING_BIT or BOX_BIT or BOMB_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or PIG_BIT or CANNON_BIT
                    }
                    PIG_FIRE -> {
                        imageOffset = Vector2(0.7f,0.65f)
                        entityModel = EntityModel.PIG_FIRE
                        speedScaling = 0.5f
                        physicOffset = Vector2(0.4f,0f)
                        physicScaling = Vector2(0.54f,0.77f)
                        extraAttackRange = 1f
                        attackDamage = (30..50)
                        critChance = 0.1f
                        density = 500f
                        score = 200
                        attackFixtureDestroyDelay = 0.4f
                        collisionRange = 3f
                        bodyType = BodyType.DynamicBody
                        attackDelay = CANNON_ATTACK_DELAY
                        attackType = AttackType.CANNON
                        maskBits = KING_BIT or BOX_BIT or BOX_BIT or BOMB_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or PIG_BIT or ROOF_BIT
                    }
                    PIG_BOMB -> {
                        imageOffset = Vector2(0.8f,0.85f)
                        entityModel = EntityModel.PIG_BOMB
                        physicScaling = Vector2(0.45f,0.55f)
                        speedScaling = 0.5f
                        critChance = 0.1f
                        density = 250f
                        score = 300
                        attackDamage = (20..30)
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
                        critChance = 0.4f
                        density = 240f
                        score = 300
                        attackDamage = (10..20)
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
                    bodyType = bodyType,
                    physicScaling = physicScaling,
                    physicOffset = physicOffset,
                    canAttack = true,
                    categoryBit = PIG_BIT,
                    maskBits = maskBits,
                    density = density,
                    score = score,
                    critChance = critChance,
                    attackDamage = attackDamage,
                    attackFixtureDestroyDelay = attackFixtureDestroyDelay,
                    attackType = attackType,
                    attackDelay = attackDelay,
                    speedScaling = speedScaling,
                    collisionRange = collisionRange,
                    lifeScaling = 0.5f,
                    aiTreePath = "ai/pig.tree",
                    maxLife = 20f,
                    moveRange = spawnComp.moveRange,
                    imageScaling = imageOffset,
                    extraAttackRange = extraAttackRange
                )
            }
            BOX ->{
                SpawnConfig(
                    model = EntityModel.BOX,
                    bodyType = BodyType.DynamicBody,
                    canAttack = false,
                    imageScaling = Vector2(0.63f,0.63f),
                    physicScaling = Vector2(1f,1f),
                    physicOffset = Vector2(0f,0f),
                    categoryBit = BOX_BIT,
                    score = 100,
                    density = 3500f,
                    maskBits = BOX_BIT or AXE_BIT or GROUND_BIT or ROOF_BIT or BOMB_BIT or BOX_BIT or KING_BIT or KING_PIG_BIT or PIG_BIT or FOOT_BIT or ENEMY_FOOT_BIT or CANNON_BIT,
                    speedScaling = 0f,
                    lifeScaling = 0f,
                    moveRange = spawnComp.moveRange
                )
            }
            BOMB ->{
                SpawnConfig(
                    model = EntityModel.BOMB,
                    bodyType = BodyType.DynamicBody,
                    canAttack = false,
                    density = 5000f,
                    score = 100,
                    imageScaling = Vector2(1.5f,1.5f),
                    physicScaling = Vector2(0.25f,0.23f),
                    physicOffset = Vector2(1.27f,1.1f),
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
                    bodyType = BodyType.StaticBody,
                    canAttack = true,
                    imageScaling = Vector2(1.57f,1f),
                    physicScaling = Vector2(0.55f,0.63f),
                    physicOffset = Vector2(1.05f,0.25f),
                    categoryBit = CANNON_BIT,
                    score = 500,
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
        if (
            size == 0 ||
            spawnConfig.model == EntityModel.DOOR
        ){
            if (size >0 ) size--
            spawnComponent.run {
                world.entity { spawnEntity->
                    val imageSize = Vector2(width,height)
                    add<ImageComponent>{
                        image = FlipImage(flipX = flipX).apply {
                            setSize(imageSize.x,imageSize.y)
                            setPosition(location.x,location.y)
                        }
                    }
                    add<ScoreComponent>{
                        score = spawnConfig.score
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
                            EntityModel.DOOR->{
                                isDoor = true
                                playMode = Animation.PlayMode.NORMAL
                                frameDuration = DEFAULT_FRAME_DURATION * 2f
                                AnimationType.OPENING
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
                        if (spawnConfig.model == EntityModel.CANNON && flipX) {
                            spawnConfig.physicOffset.set(
                                0.2f,0.25f
                            )
                        }
                        offset.set(spawnConfig.physicOffset)
                        //for image physic system
                        body = physicWorld.body(spawnConfig.bodyType){

                            fixedRotation = true
                            if (spawnConfig.model != EntityModel.KING) linearDamping = 0.7f
                            position.set(location.x + size.x * 0.5f ,location.y + size.y * 0.5f)
                            userData = spawnEntity
                            box(size.x,size.y,offset){
                                isSensor = spawnConfig.model == EntityModel.DOOR
                                filter.categoryBits = spawnConfig.categoryBit
                                filter.maskBits = spawnConfig.maskBits
                                density = spawnConfig.density
                                restitution = 0f
                                friction = 0f
                                userData = if(spawnConfig.model == EntityModel.BOX) DESTRUCTABLE_BOX else spawnEntity
                            }

                            if (spawnConfig.model == EntityModel.PIG_BOX){
                                box(1.15f,1f,position = Vector2(0.55f,0.55f)){
                                    filter.categoryBits = BOX_BIT
                                    filter.maskBits = KING_BIT or GROUND_BIT or FOOT_BIT or ENEMY_FOOT_BIT
                                    userData = BOX_ON_HEAD
                                }
                            }
                            if (spawnConfig.model == EntityModel.KING){
                                imageScaleDirection = -0.5f
                                edge(1.5f,1f,1.5f,1.5f){
                                    isSensor = true
                                    userData = spawnEntity
                                    filter.categoryBits = FOOT_BIT
                                    filter.maskBits = KING_FOOT_ENABLE_COLLISION
                                }

                            }else if(spawnConfig.speedScaling != 0f && spawnConfig.model != EntityModel.BOX && spawnConfig.model != EntityModel.CANNON && spawnConfig.model != EntityModel.BOMB){
                                val x = if (spawnConfig.model == EntityModel.PIG_BOX) 0.6f else if(spawnConfig.model == EntityModel.PIG || spawnConfig.model == EntityModel.KING_PIG) 0.75f else 0.35f
                                val yVector = if(spawnConfig.model == EntityModel.PIG || spawnConfig.model == EntityModel.KING_PIG) Vector2(-0.7f,-0.3f) else Vector2(-0.7f,-0.4f)
                                edge(x,yVector.x,x,yVector.y){
                                    isSensor = true
                                    userData = spawnEntity
                                    filter.categoryBits = ENEMY_FOOT_BIT
                                    filter.maskBits = GROUND_BIT or BOX_BIT or ROOF_BIT
                                }
                            }
                        }
                    }
                    setImageOffset(spawnConfig.model)

                    if (spawnConfig.canAttack){
                        add<AttackComponent>{
                            attackState = AttackState.READY
                            if (spawnConfig.model != EntityModel.KING)
                                attackOnEnemy = false
                            criticalHitChance = spawnConfig.critChance
                            attackDamage = spawnConfig.attackDamage
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
                            takeDamage = 0
                            if(spawnConfig.model == EntityModel.KING) extraLife = 3
                        }
                    }

                    if (spawnConfig.model == EntityModel.KING){
                        add<PlayerComponent>()
                        add<PlayerStateComponent>()
                    }else if (spawnConfig.model !in setOf(EntityModel.BOX, EntityModel.BOMB, EntityModel.CANNON, EntityModel.DOOR)){
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

                    if(spawnConfig.model == EntityModel.BOX){
                        add<DestructableComponent>{
                            body = physicComponent.body
                            itemModels = listOf(EntityModel.DIAMOND,EntityModel.HEARTH,EntityModel.BOMB).shuffled().take(2)
                        }
                    }
                }
            }
            if (mapChange){
                imageComps[playerEntity!!].image.toFront()
            }
            world.remove(entity)
        }

    }

    private fun createCollisionObjects(mapObject: MapObject){
        when(mapObject){
            is RectangleMapObject ->{
                world.entity {entity->
                    add<PhysicComponent>{
                        size.set(mapObject.rectangle.width * UNIT_SCALE , mapObject.rectangle.height * UNIT_SCALE)
                        body = physicWorld.body(BodyType.StaticBody){
                            position.set(
                                (mapObject.rectangle.x + mapObject.rectangle.width/2f) * UNIT_SCALE,
                                (mapObject.rectangle.y + mapObject.rectangle.height/2f) * UNIT_SCALE
                            )
                            userData = entity
                            box(size.x,size.y){
                                filter.categoryBits = if(mapObject.name == "Roof") ROOF_BIT else if(mapObject.name == "Portal") PORTAL_BIT else GROUND_BIT
                                restitution = 0f
                                friction = 0f
                                userData = mapObject.properties.get("toMap")?.let {
                                    it as String
                                }?:""
                                if (mapObject.name == "Portal"){
                                    isSensor = true
                                    filter.maskBits = KING_BIT
                                }
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
                for (mapObject in objectLayer.objects){
                    val objName = mapObject.name ?: gdxError("There is no name implemented for object $mapObject")
                    val moveRange = mapObject.properties.get("moveRange")?.let {
                        it as Float
                    }
                    val identify = mapObject.properties.get("identify")?.let {
                        it as String
                    }
                    val flipX = mapObject.properties.get("flipX")?.let {
                        it as Boolean
                    }?:false

                    if (event.entity != null && objName == KING) {
                        mapChange = true
                        playerEntity = event.entity
                        physicComps[playerEntity!!].run {
                            imageScaleDirection = -0.5f
                            body = physicWorld.body(BodyType.DynamicBody){
                                fixedRotation = true
                                position.set(mapObject.x * UNIT_SCALE, mapObject.y * UNIT_SCALE)
                                userData = playerEntity
                                box(mapObject.width * UNIT_SCALE * 0.2f,mapObject.height * UNIT_SCALE * 0.4f,Vector2(1.52f,2f)){
                                    filter.categoryBits = KING_BIT
                                    density = 100f
                                    friction = 0f
                                    restitution = 0f
                                    userData = playerEntity
                                    filter.maskBits = ITEM_BIT or PORTAL_BIT or KING_PIG_BIT or CANNON_BIT or PIG_BIT or GROUND_BIT or BOX_BIT or COLLISION_DETECT_BIT or ENEMY_AXE_BIT or BOMB_BIT
                                }

                                edge(1.5f,1f,1.5f,1.5f){
                                    isSensor = true
                                    userData = playerEntity
                                    filter.categoryBits = FOOT_BIT
                                    filter.maskBits = KING_FOOT_ENABLE_COLLISION
                                }
                            }
                        }
                        continue
                    }
                    world.entity {
                        add<SpawnComponent>{
                            name = objName
                            this.flipX = flipX
                            width = mapObject.width * UNIT_SCALE
                            height = mapObject.height * UNIT_SCALE
                            this.identify = identify?: ""
                            this.moveRange = moveRange?:0f
                            location.set(
                                mapObject.x * UNIT_SCALE,
                                mapObject.y * UNIT_SCALE
                            )
                        }
                    }
                }

                val portalLayer = event.map.layer("Portals")
                portalLayer.objects.forEach {mapObject->
                    if (mapObject.name == "Portal"){
                        createCollisionObjects(mapObject)
                    }
                    if(mapObject.name == "Gate"){
                        size ++
                        world.entity {
                            add<SpawnComponent>{
                                name = mapObject.name
                                width = mapObject.width * UNIT_SCALE
                                height = mapObject.height * UNIT_SCALE
                                location.set(
                                    mapObject.x * UNIT_SCALE,
                                    mapObject.y * UNIT_SCALE
                                )
                            }
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

    private fun setImageOffset(entityModel: EntityModel){
        physicComponent.imageOffset.set(
            physicComponent.size.x * 0.5f,
            0f
        )
        physicComponent.flipImageOffset.set(
            physicComponent.size.x * 0.13f,0f
        )
        when(entityModel){
           EntityModel.CANNON ->{
                physicComponent.imageOffset.set(
                    physicComponent.size.x * 0.5f,
                    0f
                )
               physicComponent.flipImageOffset.set(
                   physicComponent.size.x *  0.5f,0f
               )
            }
            EntityModel.KING ->{
                physicComponent.imageOffset.set(
                    physicComponent.size.x * 0.5f,
                    0f
                )
                physicComponent.flipImageOffset.set(
                    physicComponent.size.x * 1.4f,
                    0f
                )
            }

            EntityModel.PIG -> {
                physicComponent.imageOffset.set(
                    physicComponent.size.x * 0.5f,
                    0f
                )
                physicComponent.flipImageOffset.set(
                    physicComponent.size.x * 0.13f,
                    0f
                )
            }
            EntityModel.KING_PIG -> {
                physicComponent.imageOffset.set(
                     physicComponent.size.x * 0.5f,
                    -0.1f
                )
                physicComponent.flipImageOffset.set(
                    physicComponent.size.x * 0.4f,0f
                )
            }
            EntityModel.PIG_BOMB ->{
                physicComponent.imageOffset.set(
                    physicComponent.size.x * 0.5f,
                    0f
                )
                physicComponent.flipImageOffset.set(
                    physicComponent.size.x * 0.8f,
                    0f
                )
            }
            EntityModel.PIG_BOX_INSIDE ->{
                physicComponent.imageOffset.set(
                    physicComponent.size.x * 0.5f,
                    0f
                )
                physicComponent.flipImageOffset.set(
                    physicComponent.size.x * 0.5f,
                    0f
                )
            }

            EntityModel.PIG_FIRE ->{
                physicComponent.imageOffset.set(
                    physicComponent.size.x * 0.5f,
                    0f
                )
                physicComponent.flipImageOffset.set(
                    physicComponent.size.x * 0.5f,
                    -0.3f
                )
            }
            else -> Unit
        }
    }


    companion object{
        const val BOX_ON_HEAD = "BoxOnHead"
        const val DESTRUCTABLE_BOX = "DestructableBox"
    }

}

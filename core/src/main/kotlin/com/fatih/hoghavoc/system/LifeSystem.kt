package com.fatih.hoghavoc.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.TimeUtils
import com.fatih.hoghavoc.utils.*
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.EnemyDamageEvent
import com.fatih.hoghavoc.events.EnemyHitPlayerEvent
import com.fatih.hoghavoc.events.PlayerDamageEvent
import com.fatih.hoghavoc.events.PlayerHitEnemyEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.assets.disposeSafely
import ktx.math.random
import kotlin.math.abs

@AllOf([LifeComponent::class])
class LifeSystem(
    private val lifeComps: ComponentMapper<LifeComponent>,
    private val deadComps : ComponentMapper<DeadComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val animComps : ComponentMapper<AnimationComponent>,
    private val attackComps : ComponentMapper<AttackComponent>,
    private val moveComps : ComponentMapper<MoveComponent>,
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val gameStage: Stage
) : IteratingSystem() , EventListener{

    private lateinit var lifeComponent: LifeComponent
    private lateinit var physicComponent: PhysicComponent
    private lateinit var animationComponent: AnimationComponent
    private val entityHitTimer = hashMapOf<Entity,Long>()
    private var text = ""

    private val damageFont = BitmapFont(Gdx.files.internal("font/damage.fnt")).apply {
        data.setScale(0.5f)
    }
    private val floatingTextStyle = Label.LabelStyle(damageFont, Color.WHITE)

    override fun onTickEntity(entity: Entity) {
        lifeComponent = lifeComps[entity]
        physicComponent = physicComps[entity]
        animationComponent = animComps[entity]
        lifeComponent.run {
            if (takeDamage > 0f && life > 0f){
                getHit = true
                life -= takeDamage
                text =  if (critHit) "${lifeComponent.takeDamage} Crit!" else lifeComponent.takeDamage.toString()
                critHit = false
                floatingText(text,physicComponent.body.position,physicComponent.size)
                if (entity in playerComps){
                    gameStage.fireEvent(PlayerDamageEvent(entity))
                }else{
                    gameStage.fireEvent(EnemyDamageEvent(entity))
                }
                takeDamage = 0
            }
            if (isDead){
                moveComps.getOrNull(entity)?.root = true
                delay -= deltaTime
                if (delay <= 0f){
                    animationComponent.nextAnimation(AnimationType.DEAD,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION )
                    configureEntity(entity){entity->
                        if (entity !in deadComps){
                            deadComps.add(entity){
                                reviveTime = 5f
                            }
                        }
                        physicComponent.body.fixtureList.forEach { fixture ->
                           fixture.filterData.maskBits = if (!fixture.isSensor) GROUND_BIT else NOTHING_BIT
                        }
                        lifeComps.remove(entity)
                    }
                    delay = DEAD_DELAY
                }
            }
        }
    }

    private fun floatingText(text:String,position:Vector2,size:Vector2){
        world.entity {
            add<FloatingTextComponent>{
                textLocation.set(position.x + size.x * (0.5f..1.5f).random(),position.y + size.y * (0.8f..2f).random())
                lifeSpan = 2f
                label = Label(text,floatingTextStyle)
            }
        }
    }

    override fun handle(event: Event): Boolean {
        return when(event) {
            is PlayerHitEnemyEvent ->{
                val time = entityHitTimer.getOrPut(event.enemyEntity){
                    lifeComps.getOrNull(event.enemyEntity)?.let {lifeComponent->
                        lifeComponent.takeDamage = attackComps[event.playerEntity].let {
                            if (Math.random() < it.criticalHitChance) it.attackDamage.random() * 2 else it.attackDamage.random()
                        }
                    }
                    TimeUtils.millis()
                }
                if (TimeUtils.millis() - time >= TIME_BETWEEN_ATTACKS){
                    lifeComps.getOrNull(event.enemyEntity)?.let{lifeComponent->
                        lifeComponent.takeDamage= attackComps[event.playerEntity].let {
                            if (Math.random() < it.criticalHitChance) it.attackDamage.random()* 2 else it.attackDamage.random()
                        }
                    }
                }
                entityHitTimer[event.enemyEntity] = TimeUtils.millis()
                true
            }

            is EnemyHitPlayerEvent -> {
                event.enemyEntity?.let {
                    lifeComps.getOrNull(event.playerEntity)?.let{lifeComponent->
                        lifeComponent.run {
                            takeDamage= attackComps.getOrNull(event.enemyEntity)?.let {
                                it.attackDamage.random() * if ((0f..1f).random() < it.criticalHitChance) {
                                    critHit = true
                                    2
                                } else 1
                            }?: 0
                        }
                    }
                }
                true
            }
            else -> false
        }
    }

    override fun onDispose() {
        damageFont.disposeSafely()
    }
}

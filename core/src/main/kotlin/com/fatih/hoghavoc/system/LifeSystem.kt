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
            if (getHit){
                hitDelay -= deltaTime
                if (hitDelay<=0f){
                    getHit = false
                    hitDelay = HIT_DELAY
                }
            }
            if (takeDamage > 0f && life > 0f){
                getHit = true
                life -= takeDamage
                text =  if (takeDamage > 5f) "${lifeComponent.takeDamage.toInt()} Crit!" else lifeComponent.takeDamage.toInt().toString()
                floatingText(text,physicComponent.body.position,physicComponent.size)
                if (entity in playerComps){
                    gameStage.fireEvent(PlayerDamageEvent(entity))
                }else{
                    gameStage.fireEvent(EnemyDamageEvent(entity))
                }
                takeDamage = 0f
            }
            if (isDead){
                getHit = false
                moveComps.getOrNull(entity)?.root = true
                delay -= deltaTime
                if (delay <= 0f){
                    animationComponent.nextAnimation(AnimationType.DEAD,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION * 3f)
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
                textLocation.set(position.x + size.x * 0.4f,position.y + size.y * 0.5f)
                lifeSpan = 2f
                label = Label(text,floatingTextStyle)
            }
        }
    }

    override fun handle(event: Event): Boolean {
        return when(event) {
            is PlayerHitEnemyEvent ->{
                animComps.getOrNull(event.enemyEntity)?.nextAnimation(AnimationType.HIT,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION )
                val time = entityHitTimer.getOrPut(event.enemyEntity){
                    lifeComps.getOrNull(event.enemyEntity)?.let {lifeComponent->
                        lifeComponent.takeDamage = attackComps[event.playerEntity].let {
                            if (Math.random() < it.criticalHitChance) it.attackDamage.random().toFloat() * 2 else it.attackDamage.random().toFloat()
                        }
                    }
                    TimeUtils.millis()
                }
                if (TimeUtils.millis() - time >= TIME_BETWEEN_ATTACKS){
                    lifeComps.getOrNull(event.enemyEntity)?.let{lifeComponent->
                        lifeComponent.takeDamage= attackComps[event.playerEntity].let {
                            if (Math.random() < it.criticalHitChance) it.attackDamage.random().toFloat() * 2 else it.attackDamage.random().toFloat()
                        }
                    }
                }
                entityHitTimer[event.enemyEntity] = TimeUtils.millis()
                true
            }

            is EnemyHitPlayerEvent -> {
                animComps.getOrNull(event.playerEntity)?.nextAnimation(AnimationType.HIT,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION)
                lifeComps.getOrNull(event.playerEntity)?.let{lifeComponent->
                    lifeComponent.takeDamage= attackComps[event.enemyEntity].let {
                        if (Math.random() < it.criticalHitChance) it.attackDamage.random().toFloat() * 2 else it.attackDamage.random().toFloat()
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
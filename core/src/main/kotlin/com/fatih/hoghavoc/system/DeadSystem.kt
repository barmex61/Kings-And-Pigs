package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.TimeUtils
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.FinalScoreEvent
import com.fatih.hoghavoc.events.ScoreEvent
import com.fatih.hoghavoc.utils.fireEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([DeadComponent::class])
class DeadSystem(
    private val deadComps : ComponentMapper<DeadComponent>,
    private val animComps : ComponentMapper<AnimationComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val attackComps : ComponentMapper<AttackComponent>,
    private val scoreComps : ComponentMapper<ScoreComponent>,
    private val attackFixtureComps : ComponentMapper<AttackFixtureComponent>,
    private val physicWorld : World,
    private val gameStage : Stage,
    private val playerComps : ComponentMapper<PlayerComponent>
) : IteratingSystem(){

    private lateinit var deadComponent: DeadComponent
    private lateinit var animationComponent: AnimationComponent

    override fun onTickEntity(entity: Entity) {
        deadComponent = deadComps[entity]
        animationComponent = animComps[entity]
        deadComponent.run {

            if (destroyInstantly || animationComponent.isAnimationDone(AnimationType.DEAD,Animation.PlayMode.NORMAL)){
                if (entity in playerComps){
                    gameStage.fireEvent(FinalScoreEvent())
                }
                scoreComps.getOrNull(entity)?.run{
                    gameStage.fireEvent(ScoreEvent(score))
                }
                attackFixtureComps.getOrNull(entity)?.run {
                    attackBody?.run {
                        physicWorld.destroyBody(this)
                    }
                    explodeBody?.run {
                        physicWorld.destroyBody(this)
                    }
                    boxPiecesHashMap?.values?.forEach { body ->
                        physicWorld.destroyBody(body)
                    }
                }
                attackComps.getOrNull(entity)?.run {
                    meleeAttackBody?.let {
                        physicWorld.destroyBody(it)
                    }
                }

                physicWorld.destroyBody(physicComps[entity].body)
                world.remove(entity)
            }
        }
    }
}

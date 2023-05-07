package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.component.*
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
    private val gameStage:Stage,
    private val physicWorld : World
) : IteratingSystem(){

    private lateinit var deadComponent: DeadComponent
    private lateinit var animationComponent: AnimationComponent

    override fun onTickEntity(entity: Entity) {
        deadComponent = deadComps[entity]
        animationComponent = animComps[entity]
        deadComponent.run {
            if (animationComponent.isAnimationDone(AnimationType.DEAD,Animation.PlayMode.NORMAL)){
                attackComps.getOrNull(entity)?.run {
                    boxAttackBody?.let {
                        physicWorld.destroyBody(it)
                    }
                    meleeAttackBody?.let {
                        physicWorld.destroyBody(it)
                    }
                    boxAttackImage?.let {
                        gameStage.root.removeActor(it)
                    }
                    boxAttackImage = null
                }
                physicWorld.destroyBody(physicComps[entity].body)
                world.remove(entity)
            }
        }
    }
}

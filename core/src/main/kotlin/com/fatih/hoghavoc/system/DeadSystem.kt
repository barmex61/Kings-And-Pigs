package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.TimeUtils
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
    private val attackFixtureComps : ComponentMapper<AttackFixtureComponent>,
    private val physicWorld : World
) : IteratingSystem(){

    private lateinit var deadComponent: DeadComponent
    private lateinit var animationComponent: AnimationComponent

    override fun onTickEntity(entity: Entity) {
        deadComponent = deadComps[entity]
        animationComponent = animComps[entity]
        deadComponent.run {
            if (animationComponent.isAnimationDone(AnimationType.DEAD,Animation.PlayMode.NORMAL)){
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
                physicComps[entity].body?.let {
                    physicWorld.destroyBody(physicComps[entity].body)
                }
                world.remove(entity)
            }
        }
    }
}

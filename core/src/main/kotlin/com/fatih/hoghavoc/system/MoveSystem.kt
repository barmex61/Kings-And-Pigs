package com.fatih.hoghavoc.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.JumpEvent
import com.fatih.hoghavoc.utils.fireEvent
import com.github.quillraven.fleks.*
import ktx.math.component1
import ktx.math.component2
import kotlin.math.abs

@AllOf([MoveComponent::class])
class MoveSystem(
    private val moveComps : ComponentMapper<MoveComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val enemyComps : ComponentMapper<EnemyComponent>,
    private val gameStage : Stage
) : IteratingSystem() {

    private lateinit var moveComponent: MoveComponent
    private lateinit var physicComponent: PhysicComponent
    private lateinit var imageComponent: ImageComponent

    override fun onTickEntity(entity: Entity) {

        moveComponent = moveComps[entity]
        physicComponent = physicComps[entity]
        imageComponent = imageComps[entity]
        physicComponent.run {
            val mass = body.mass
            val (velX, velY) = body.linearVelocity
            if (!(!impulse.isZero && entity in enemyComps) ){
                moveComponent.run {
                    if (fireEvent){
                        gameStage.fireEvent(JumpEvent())
                        fireEvent = false
                    }
                    if ((cos == 0f && sin == 0f) || root ) {
                        impulse.set(
                            mass * (0f - velX),
                            mass * (0f - velY)
                        )
                        if (moveComponent.root) {
                            impulse.y = -abs(impulse.y * 0.01f)
                            return
                        }
                    }
                    impulse.set(
                        mass * (speed.x * cos - velX * 0.5f),
                        if (canJump && abs(body.linearVelocity.y) <= 0.05f || body.linearVelocity.y == 0f) {
                            mass * (speed.y * sin) * 10f
                        } else 0f
                    )
                    if (cos != 0f) {
                        imageComponent.image.flipX = if (entity in enemyComps)  cos > 0f else cos < 0f
                    }
                }

            }
        }
    }
}

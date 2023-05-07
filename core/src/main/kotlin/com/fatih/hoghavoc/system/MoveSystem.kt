package com.fatih.hoghavoc.system

import com.fatih.hoghavoc.component.*
import com.github.quillraven.fleks.*
import ktx.math.component1
import ktx.math.component2
import kotlin.math.abs

@AllOf([MoveComponent::class])
class MoveSystem(
    private val moveComps : ComponentMapper<MoveComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val enemyComps : ComponentMapper<EnemyComponent>
) : IteratingSystem() {

    private lateinit var moveComponent: MoveComponent
    private lateinit var physicComponent: PhysicComponent
    private lateinit var imageComponent: ImageComponent
    private var timer = 0f

    override fun onTickEntity(entity: Entity) {
        moveComponent = moveComps[entity]
        physicComponent = physicComps[entity]
        imageComponent = imageComps[entity]
        val mass = physicComponent.body.mass
        val (velX, velY) = physicComponent.body.linearVelocity
        if (!(!physicComponent.impulse.isZero && entity in enemyComps) ){
            if (!moveComponent.canJump && physicComponent.body.linearVelocity.y == 0f) {
                timer += deltaTime
            }
        if ((moveComponent.cos == 0f && moveComponent.sin == 0f) || moveComponent.root ) {
            physicComponent.impulse.set(
                mass * (0f - velX),
                mass * (0f - velY)
            )
            if (moveComponent.root) {
                physicComponent.impulse.y = -abs(physicComponent.impulse.y * 0.01f)
                return
            }
        }
        physicComponent.impulse.set(
            mass * (moveComponent.speed.x * moveComponent.cos - velX * 0.5f),
            if ((moveComponent.canJump || timer >= 0.15f) && abs(physicComponent.body.linearVelocity.y) <= 0.05f) {
                mass * (moveComponent.speed.y * moveComponent.sin) * 10f
            } else 0f
        )
        if (moveComponent.sin > 0f && timer >= 0.15f) {
            timer = 0f
        }
        if (moveComponent.cos != 0f) {
            imageComponent.image.flipX = if (entity in enemyComps)  moveComponent.cos > 0f else moveComponent.cos < 0f

        }
    }
}
}

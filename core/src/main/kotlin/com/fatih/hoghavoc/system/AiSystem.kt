package com.fatih.hoghavoc.system

import com.badlogic.gdx.utils.TimeUtils
import com.fatih.hoghavoc.component.AiComponent
import com.fatih.hoghavoc.component.DeadComponent
import com.github.quillraven.fleks.*

@AllOf([AiComponent::class])
@NoneOf([DeadComponent::class])
class AiSystem(
    private val aiComps : ComponentMapper<AiComponent>
) : IteratingSystem() {

    private lateinit var aiComponent: AiComponent


    override fun onTickEntity(entity: Entity) {
        aiComponent = aiComps[entity]
        aiComponent.run {
            behaviorTree.step()
        }
    }
}

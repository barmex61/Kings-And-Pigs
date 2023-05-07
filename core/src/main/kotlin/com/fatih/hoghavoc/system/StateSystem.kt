package com.fatih.hoghavoc.system

import com.fatih.hoghavoc.component.PlayerStateComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([PlayerStateComponent::class])
class StateSystem(
    private val stateComps : ComponentMapper<PlayerStateComponent>
) : IteratingSystem(){

    private lateinit var playerStateComponent: PlayerStateComponent

    override fun onTickEntity(entity: Entity) {
        playerStateComponent = stateComps[entity]
        playerStateComponent.run {
            if (nextState != stateMachine.currentState){
                stateMachine.changeState(nextState)
            }
            stateMachine.update()
        }
    }
}

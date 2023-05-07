package com.fatih.hoghavoc.component

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.fatih.hoghavoc.ai.PlayerAiEntity
import com.fatih.hoghavoc.ai.EntityState
import com.fatih.hoghavoc.ai.PlayerState
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class PlayerStateComponent(
    var nextState : EntityState = PlayerState.DOOR_OUT,
    val stateMachine : DefaultStateMachine<PlayerAiEntity,EntityState> = DefaultStateMachine(),
){
    companion object{
        class StateComponentListener(
            private val world: World
        ) : ComponentListener<PlayerStateComponent>{
            override fun onComponentAdded(entity: Entity, component: PlayerStateComponent) {
                component.stateMachine.owner = PlayerAiEntity(world,entity)
            }

            override fun onComponentRemoved(entity: Entity, component: PlayerStateComponent) = Unit
        }
    }
}

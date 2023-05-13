package com.fatih.hoghavoc.ai

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.utils.DEFAULT_FRAME_DURATION
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class PlayerAiEntity(
    private val world: World,
    val entity: Entity,
    animComps : ComponentMapper<AnimationComponent> = world.mapper(),
    attackComps : ComponentMapper<AttackComponent> = world.mapper(),
    playerStateComps : ComponentMapper<PlayerStateComponent> = world.mapper(),
    lifeComps : ComponentMapper<LifeComponent> = world.mapper(),
    moveComps : ComponentMapper<MoveComponent> = world.mapper(),
    physicComps : ComponentMapper<PhysicComponent> = world.mapper()
) {


    val animationComponent = animComps[entity]
    private val attackComponent = attackComps[entity]
    private val playerStateComponent = playerStateComps[entity]
    private val lifeComponent = lifeComps[entity]
    private val moveComponent = moveComps[entity]
    private val physicComponent = physicComps[entity]
    var moveIn: Boolean
        get() = moveComponent.moveIn
        set(value) {
            moveComponent.moveIn = value
        }

    val isDead: Boolean
        get() = lifeComponent.isDead

    val getHit: Boolean
        get() = lifeComponent.getHit

    val wantsToResurrect: Boolean
        get() = lifeComponent.wantsToResurrect

    val wantsToAttack: Boolean
    get() {
         return if (attackComponent.doAttack && attackComponent.attackState == AttackState.READY) {
             attackComponent.attackState = AttackState.ATTACK
             true
         }else false
     }

    val isJumping: Boolean
        get() = physicComponent.body.linearVelocity.y > 1.2f

    val isFalling : Boolean
        get() = physicComponent.body.linearVelocity.y < -1.2f

    val isRunning : Boolean
        get() = moveComponent.cos != 0f && attackComponent.attackState == AttackState.READY

    val animDone: Boolean
        get() = animationComponent.isAnimationDone(AnimationType.ATTACK)


    var fireEvent : Boolean = false
        get() = attackComponent.fireEvent
        set(value) {
            attackComponent.fireEvent = value
            field = value
        }

    var fireJumpEvent : Boolean = false
        get() = moveComponent.fireEvent
        set(value) {
            moveComponent.fireEvent = value
            field = value
        }

    fun startAnimation(animationType: AnimationType,playMode: PlayMode,frameDuration : Float = DEFAULT_FRAME_DURATION ) {
        animationComponent.nextAnimation(animationType,playMode,frameDuration)
    }

    fun changeState(playerState: PlayerState,fromAttackState: Boolean = false , toPreviousState : Boolean = false) {
        if (toPreviousState){
            playerStateComponent.nextState = playerStateComponent.stateMachine.previousState
            playerStateComponent.stateMachine.changeState(playerStateComponent.nextState)
        }else{
            if (fromAttackState){
                playerStateComponent.nextState = playerStateComponent.stateMachine.previousState
                playerStateComponent.stateMachine.changeState(playerStateComponent.nextState)
            }else{
                playerStateComponent.nextState = playerState
            }
        }
    }

    fun root(enabled: Boolean) {
        moveComponent.root = enabled
    }

    fun setTexture(textureType: TextureType) {
        animationComponent.nextTexture(textureType)
    }

    fun resetAttackState() {
        attackComponent.resetState = true
        lifeComponent.getHit = false
    }

}

package com.fatih.hoghavoc.component

import com.badlogic.gdx.physics.box2d.Body
import com.fatih.hoghavoc.actors.FlipImage


enum class AttackState {
    READY,ATTACK
}

enum class AttackType{
    BOX,CANNON,BOMB,MELEE_ATTACK
}

class AttackComponent(
    var doAttack : Boolean = false,
    var attackDamage : IntRange = (1..5),
    var attackRange : Float = 0f,
    var criticalHitChance: Float = 0f,
    var delay : Float = 1f,
    var maxDelay : Float = 1f,
    var attackState : AttackState = AttackState.READY,
    var attackOnEnemy : Boolean = true,
    var attackType : AttackType = AttackType.MELEE_ATTACK,
    var meleeAttackBody : Body? = null,
    var attackDone :Boolean = false,
    var resetState : Boolean = false,
    var fireEvent: Boolean = false
){

    val isReady : Boolean
        get() = attackState == AttackState.READY

    val isAttacking : Boolean
        get() = attackState == AttackState.ATTACK
}

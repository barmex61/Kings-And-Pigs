package com.fatih.hoghavoc.component

import com.badlogic.gdx.physics.box2d.Body
import com.fatih.hoghavoc.actors.FlipImage


enum class AttackState {
    READY,ATTACK
}

enum class AttackType{
    BOX_ATTACK,FIRE_ATTACK,BOMB_ATTACK,MELEE_ATTACK
}

class AttackComponent(
    var attackDamage : IntRange = (1..5),
    var attackRange : Float = 0f,
    val criticalHitChance: Float = 0.5f,
    var delay : Float = 1f,
    var maxDelay : Float = 1f,
    var attackState : AttackState = AttackState.READY,
    var attackOnEnemy : Boolean = true,
    var attackType : AttackType = AttackType.MELEE_ATTACK,
    var meleeAttackBody : Body? = null,
    var boxAttackBody : Body? = null,
    var boxAttackImage : FlipImage? = null,
    var bombAttackBody : Body? = null,
    var bombAttackImage : FlipImage? = null

){

    val isReady : Boolean
        get() = attackState == AttackState.READY

    val isAttacking : Boolean
        get() = attackState == AttackState.ATTACK
}

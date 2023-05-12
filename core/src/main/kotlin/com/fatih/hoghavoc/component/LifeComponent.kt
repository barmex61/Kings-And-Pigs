package com.fatih.hoghavoc.component

import com.fatih.hoghavoc.utils.DEAD_DELAY

class LifeComponent(
    var maxLife : Float = 30f,
    var life : Float = 30f,
    var regenerationSpeed : Float = 0f,
    var takeDamage : Int = 0,
    var delay : Float = DEAD_DELAY,
    var getHit : Boolean = false,
    var critHit : Boolean = false
) {

    val isDead : Boolean
        get() = life <= 0f

    val wantsToResurrect : Boolean
        get() = life > 0f
}

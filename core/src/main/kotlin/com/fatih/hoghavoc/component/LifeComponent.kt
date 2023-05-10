package com.fatih.hoghavoc.component

import com.fatih.hoghavoc.utils.DEAD_DELAY
import com.fatih.hoghavoc.utils.HIT_DELAY

class LifeComponent(
    var maxLife : Float = 30f,
    var life : Float = 30f,
    var regenerationSpeed : Float = 0f,
    var takeDamage : Float = 0f,
    var delay : Float = DEAD_DELAY,
    var getHit : Boolean = false
) {

    val isDead : Boolean
        get() = life <= 0f

    val wantsToResurrect : Boolean
        get() = life > 0f
}

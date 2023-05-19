package com.fatih.hoghavoc.component

import com.fatih.hoghavoc.utils.DEAD_DELAY

class LifeComponent(
    var maxLife : Float = 30f,
    var life : Float = 30f,
    var regenerationSpeed : Float = 0f,
    var takeDamage : Int = 0,
    var delay : Float = DEAD_DELAY,
    var getHit : Boolean = false,
    var critHit : Boolean = false,
    var extraLife : Int = 0,
    var fireExtraLifeEvent : Boolean = false,
    var fireHealEvent : Boolean = false,
    var resurrectTimer : Float = 7f
) {

    val deadState : Boolean
        get() = life <= 0

    val isDead : Boolean
        get() = extraLife == 0 && life <= 0

    val wantsToResurrect : Boolean
        get() = extraLife > 0
}

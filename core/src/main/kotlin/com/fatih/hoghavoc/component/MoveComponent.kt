package com.fatih.hoghavoc.component

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import ktx.math.vec2

data class MoveComponent(
    var cos : Float = 0f,
    var sin : Float = 0f,
    var speed : Vector2 = vec2(2f,1f),
    var root : Boolean = false,
    var canJump : Boolean = true,
    var moveRange : Float = 0f,
    var timeBetweenJumps : Long = TimeUtils.millis()
)


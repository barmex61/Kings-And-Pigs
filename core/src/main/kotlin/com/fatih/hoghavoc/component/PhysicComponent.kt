package com.fatih.hoghavoc.component

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import ktx.math.vec2

class PhysicComponent (
    val previousPosition : Vector2 = vec2(),
    val impulse : Vector2 = vec2(),
    val offset : Vector2 = vec2(),
    var size : Vector2 = vec2(),
    var imageScaleDirection : Float = 1f,
    var imageOffset : Vector2 = vec2()
    ){
    lateinit var body : Body
}

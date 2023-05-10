package com.fatih.hoghavoc.component

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Qualifier
import ktx.math.random
import ktx.math.vec2

class FloatingTextComponent(
    val textLocation : Vector2 = vec2(),
    val textTarget : Vector2 = vec2(),
    var lifeSpan : Float = 1f,
    var uiLocation : Vector2 = vec2(),
    val uiTarget : Vector2 = vec2()
) {
    lateinit var label : Label

    companion object{
        class FloatingTextComponentListener(
            @Qualifier("uiStage") private val uiStage : Stage
        ) : ComponentListener<FloatingTextComponent>{
            override fun onComponentAdded(entity: Entity, component: FloatingTextComponent) {
                uiStage.addActor(component.label)
                component.label.addAction(fadeOut(component.lifeSpan , Interpolation.pow3OutInverse))
                component.textTarget.set(
                    component.textLocation.x + (-2.5f..2.5f).random(),
                    component.textLocation.y + (1f..2.5f).random()
                )
            }

            override fun onComponentRemoved(entity: Entity, component: FloatingTextComponent) {
                component.label.addAction(sequence(
                    fadeIn(component.lifeSpan, Interpolation.pow3OutInverse),
                    removeActor(component.label).apply {
                        uiStage.root.removeActor(component.label)
                    }
                ))
            }
        }
    }
}

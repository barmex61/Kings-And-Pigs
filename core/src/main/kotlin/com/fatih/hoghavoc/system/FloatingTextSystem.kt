package com.fatih.hoghavoc.system

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor
import com.fatih.hoghavoc.component.FloatingTextComponent
import com.github.quillraven.fleks.*

@AllOf([FloatingTextComponent::class])
class FloatingTextSystem(
    @Qualifier("uiStage") private val uiStage : Stage,
    private val gameStage : Stage,
    private val textComps : ComponentMapper<FloatingTextComponent>
) : IteratingSystem(){

    private lateinit var floatingTextComponent: FloatingTextComponent

    override fun onTickEntity(entity: Entity) {
        floatingTextComponent = textComps[entity]
        floatingTextComponent.run {

            if (lifeSpan <= 0f){
                label.addAction(Actions.sequence(
                    Actions.fadeIn(1f, Interpolation.pow3OutInverse) ,
                    delay(0.5f, removeActor(label).also {
                        gameStage.root.removeActor(label)
                    })
                ))

                world.remove(entity)
                return
            }
            lifeSpan -= deltaTime
            if (uiLocation.isZero  ){
                uiLocation.set(textLocation)
                gameStage.viewport.project(uiLocation)
                uiStage.viewport.unproject(uiLocation)
            }
            if (uiTarget.isZero ){
                uiTarget.set(textTarget)
                gameStage.viewport.project(uiTarget)
                uiStage.viewport.unproject(uiTarget)
            }

            uiLocation.set(uiLocation.interpolate(uiTarget,lifeSpan * deltaTime * 5f, Interpolation.pow3OutInverse))
            label.setPosition(uiLocation.x, uiStage.viewport.worldHeight - uiLocation.y)
        }
    }
}

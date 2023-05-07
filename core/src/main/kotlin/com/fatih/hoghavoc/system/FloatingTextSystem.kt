package com.fatih.hoghavoc.system

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
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

            uiLocation.interpolate(uiTarget,lifeSpan * deltaTime * 10f, Interpolation.smooth2)
            label.setPosition(uiLocation.x, uiStage.viewport.worldHeight - uiLocation.y)
        }
    }
}

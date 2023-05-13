package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.fatih.hoghavoc.component.DialogComponent
import com.fatih.hoghavoc.component.EnemyComponent
import com.fatih.hoghavoc.component.ImageComponent
import com.fatih.hoghavoc.component.PhysicComponent
import com.fatih.hoghavoc.utils.DIALOG_DURATION
import com.github.quillraven.fleks.*

@AllOf([DialogComponent::class])
class DialogSystem(
    private val dialogComps:ComponentMapper<DialogComponent>,
    private val textureAtlas: TextureAtlas,
    private val gameStage:Stage,
    private val physicComps : ComponentMapper<PhysicComponent>
) : IteratingSystem() {


    private lateinit var dialogComponent : DialogComponent
    private val drawableHashMap = hashMapOf<String,TextureRegionDrawable>()
    private lateinit var physicComponent: PhysicComponent

    private fun getDrawable(drawablePath : String) : TextureRegionDrawable = drawableHashMap.getOrPut(drawablePath){
        val frame = textureAtlas.findRegion(drawablePath)
        TextureRegionDrawable(frame)
    }

    override fun onTickEntity(entity: Entity) {
        physicComponent = physicComps[entity]
        dialogComponent = dialogComps[entity]
        dialogComponent.run {
            if (showDialog && dialogTimer > 0f){
                if (dialogTimer == DIALOG_DURATION){
                    gameStage.addActor(dialogImage)
                }
                if (nextDialog.isNotEmpty()){
                    dialogImage.drawable = getDrawable(nextDialog)

                }
                dialogImage.run {
                    setPosition(physicComponent.body.position.x - width/1.5f , physicComponent.body.position.y + height/1.5f)
                }
                dialogTimer -= deltaTime /5f
            }else if (dialogTimer <= 0f){
                dialogTimer = DIALOG_DURATION
                showDialog = false
                dialogImage.actions.clear()
                configureEntity(entity){
                    dialogComps.remove(it)
                }
            }
        }
    }

}

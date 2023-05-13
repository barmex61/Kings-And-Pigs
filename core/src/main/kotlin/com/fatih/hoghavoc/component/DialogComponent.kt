package com.fatih.hoghavoc.component

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.actors.FlipImage
import com.fatih.hoghavoc.utils.DIALOG_DURATION
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity

enum class DialogType(val chance: Float){
    QUESTION(0.7f),ATTACK(0.8f),BOOM(1f),DEAD(0.6f),HELLO(0.8f),HI(0.7f),IN(1f),LOSER(0.5f),NO(0.7f),OUT(1f),WTF(1f);
    val key : String = this.name.lowercase()
}

class DialogComponent(
    var dialogTimer : Float = DIALOG_DURATION,
    var showDialog : Boolean = false,
    var dialogType : DialogType = DialogType.QUESTION,
    var nextDialog : String = "",
) {
    lateinit var dialogImage : FlipImage


    fun changeDialog(dialogType : DialogType){
        if (!showDialog){
            showDialog = true
            this.dialogType = dialogType
            nextDialog = dialogType.key
        }
    }

    companion object{
        class DialogComponentListener(
            private val gameStage:Stage
        ) : ComponentListener<DialogComponent>{
            override fun onComponentAdded(entity: Entity, component: DialogComponent) = Unit

            override fun onComponentRemoved(entity: Entity, component: DialogComponent) {
                gameStage.root.removeActor(component.dialogImage)
            }
        }
    }

}

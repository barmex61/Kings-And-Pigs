package com.fatih.hoghavoc.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.fatih.hoghavoc.ui.Drawables
import ktx.actors.alpha
import ktx.scene2d.*
import com.fatih.hoghavoc.ui.get

class AttackHud(
    skin: Skin
) : WidgetGroup() , KGroup {


    private val imageBg = Image(skin[Drawables.TOUCHPAD_BG]).apply {
        this.setSize(60f,60f)
        this.alpha = 0.25f
    }

    private val imageButton = ImageButton(skin).apply {
        this.setPosition(15f,9f)
        this.setSize(30f,42f)
        this.alpha = 0.25f
    }

    fun setAlpha(alpha:Float){
        imageBg.alpha = alpha
        imageButton.alpha = alpha
    }

    init {
        addActor(imageBg)
        addActor(imageButton)
    }
}

@Scene2dDsl
fun <T> KWidget<T>.attackHud(
    skin: Skin = Scene2DSkin.defaultSkin,
    init : AttackHud.(T) -> Unit = {}
):AttackHud = actor(AttackHud(skin),init)

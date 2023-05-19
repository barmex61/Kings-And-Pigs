package com.fatih.hoghavoc.ui.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.fatih.hoghavoc.ui.Labels
import com.fatih.hoghavoc.ui.TextButtons
import ktx.scene2d.*
import ktx.style.set

class PauseView(skin:Skin) : KTable,Table(skin) {

    val pauseLabel : Label
    val backGround : Drawable

    init {
        setFillParent(true)
        if (!skin.has(pixMapKey,TextureRegionDrawable::class.java)){
            skin[pixMapKey] = TextureRegionDrawable(
                Texture(Pixmap(1,1,Pixmap.Format.RGBA8888).apply {
                    drawPixel(0,0,Color.rgba8888(0.1f,0.1f,0.1f,0.7f))
                })
            )
        }
        backGround = skin.get(pixMapKey,TextureRegionDrawable::class.java)
        background = backGround
        pauseLabel = label("Pause",Labels.LARGE.skinKey){
            it.grow()
            this.setAlignment(Align.center)
        }
    }


    companion object{
        private val pixMapKey = "PixMapKey"
    }
}
@Scene2dDsl
fun <T> KWidget<T>.pauseView(
    skin: Skin = Scene2DSkin.defaultSkin,
    init : PauseView.(T) -> Unit
):PauseView = actor(PauseView(skin),init)

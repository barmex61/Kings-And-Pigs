package com.fatih.hoghavoc.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.fatih.hoghavoc.ui.Drawables
import ktx.scene2d.*
import com.fatih.hoghavoc.ui.get
import com.fatih.hoghavoc.ui.widget.*
import ktx.actors.alpha

class MenuView: Table(Scene2DSkin.defaultSkin), KTable {

    var bannerHud : BannerHud
    var htpHud : HowToPlayHud? = null
    var hsHud : HighScoreHud? = null

    init {
        setFillParent(true)
        center()
        background = skin[Drawables.MENU_BG]
        bannerHud =  bannerHud{
            it.padRight(200f).padBottom(200f)
            alpha = 0f
        }
    }

    fun addListeners(listener : InputListener){
        bannerHud.htpLabel.addListener(listener)
        bannerHud.scoreLabel.addListener(listener)
        bannerHud.playLabel.addListener(listener)
    }

    fun addActions(){
        bannerHud.addAction(
            Actions.sequence(Actions.moveBy(0f,100f,0.05f, Interpolation.pow3OutInverse),
                fadeIn(1f, Interpolation.smooth2),
                Actions.moveBy(0f,-100f,1f, Interpolation.pow3OutInverse),
            )
         )
    }
}

fun <T> KWidget<T>.menuView(
    init : MenuView.(T) -> Unit = {}
) : MenuView = actor(MenuView(),init)

package com.fatih.hoghavoc.ui.widget

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.fatih.hoghavoc.ui.Drawables
import com.fatih.hoghavoc.ui.Labels
import com.fatih.hoghavoc.ui.TextButtons
import ktx.scene2d.*
import com.fatih.hoghavoc.ui.get

class BannerHud : KGroup,WidgetGroup() {

    var playLabel : TextButton
    var htpLabel : TextButton
    var scoreLabel : TextButton


    init {
        image(Scene2DSkin.defaultSkin[Drawables.ITEM2]).apply {
            setSize(200f,180f)
            setPosition(0f,-200f)
        }
        playLabel = textButton(" PLAY",TextButtons.ITEMS.skinKey).apply {
            setPosition(40f,-85f)
        }

        scoreLabel = textButton("SCORE",TextButtons.ITEMS.skinKey).apply {
            setPosition(40f,-115f)
        }
        htpLabel = textButton("HOW TO PLAY",TextButtons.HTP.skinKey).apply {
            setPosition(40f,-145f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.ITEM1]).apply {
            setSize(170f,40f)
            setPosition(15f,-40f)
        }
        label("NEW GAME", Labels.MENU.skinKey).apply {
            setSize(150f,60f)
            setPosition(25f,-50f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.PLAY_ICON]){
            setSize(15f,15f)
            setPosition(32.5f,-82.5f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.SCORE_ICON]){
            setSize(15f,15f)
            setPosition(32.5f,-112.5f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.GHOST_ICON]){
            setSize(15f,15f)
            setPosition(32.5f,-142.5f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.SCORE_ICON]){
            setSize(20f,15f)
            setPosition(90f,-47.5f)
        }

    }
}

fun <T> KWidget<T>.bannerHud(
    init : BannerHud.(T) -> Unit = {}
) : BannerHud = actor(BannerHud(),init)

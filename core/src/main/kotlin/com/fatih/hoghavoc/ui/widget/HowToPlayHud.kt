package com.fatih.hoghavoc.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.fatih.hoghavoc.ui.Drawables
import com.fatih.hoghavoc.ui.ImageButtons
import com.fatih.hoghavoc.ui.TextFields
import ktx.scene2d.*
import com.fatih.hoghavoc.ui.get

class HowToPlayHud : WidgetGroup() ,KGroup {

    val backButton : ImageButton

    init {
        setFillParent(true)

        image(Scene2DSkin.defaultSkin[Drawables.ITEM2]){
            setSize(200f,180f)
            setPosition(-200f,-90f)
        }
        textField("How To Play", TextFields.HEAD.skinKey){
            setPosition(-135f,60f)
        }

        image(Scene2DSkin.defaultSkin[Drawables.P]){
            setSize(20f,20f)
            setPosition(-170f,-10f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.R]){
            setSize(20f,20f)
            setPosition(-170f,20f)
        }
        textField("Pause"){
            setPosition(-140f,-5f)
        }
        backButton = imageButton(ImageButtons.BACK.skinKey){
            setSize(20f,20f)
            setPosition(-180f,60f)
        }
        textField("Restart"){
            setPosition(-140f,25f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.SPACE]){
            setSize(50f,18f)
            setPosition(-170f,-70f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.UP]){
            setSize(20f,18f)
            setPosition(-65f,-20f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.DOWN]){
            setSize(20f,18f)
            setPosition(-65f,-40f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.RIGHT]){
            setSize(20f,18f)
            setPosition(-40f,-40f)
        }
        image(Scene2DSkin.defaultSkin[Drawables.LEFT]){
            setSize(20f,18f)
            setPosition(-90f,-40f)
        }
        textField("Movement Keys"){
            setPosition(-90f,10f)
        }
        textField("Attack Key"){
            setPosition(-170f,-45f)
        }
    }
}
fun <T> KWidget<T>.howToPlayHud(
    init : HowToPlayHud.(T) -> Unit = {}
) : HowToPlayHud = actor(HowToPlayHud(),init)

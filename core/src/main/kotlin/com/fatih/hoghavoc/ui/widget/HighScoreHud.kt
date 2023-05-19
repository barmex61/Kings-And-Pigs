package com.fatih.hoghavoc.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Json
import com.fatih.hoghavoc.HogHavoc.Companion.preferences
import com.fatih.hoghavoc.ui.*
import ktx.preferences.get
import ktx.scene2d.*

class HighScoreHud : KGroup , WidgetGroup() {

    val backButton : ImageButton
    lateinit var scoreArray: ArrayList<Int>
    private val json = Json()

    init {

        val savedScoreJson = preferences.getString("Scores","")
        if (savedScoreJson.isEmpty()){
            preferences.putString("Scores",json.toJson(arrayListOf(0,0,0,0,0)))
            preferences.flush()
        }
        scoreArray = json.fromJson(ArrayList<Int>():: class.java,savedScoreJson)
        scoreArray.sortDescending()
        image(Scene2DSkin.defaultSkin[Drawables.ITEM2]){
            setSize(200f,180f)
            setPosition(-200f,-90f)
        }

        textField("High Score", TextFields.HEAD.skinKey){
            setPosition(-135f,60f)
        }

        backButton = imageButton(ImageButtons.BACK.skinKey){
            setSize(20f,20f)
            setPosition(-180f,60f)
        }
        label("    1 -                   ${scoreArray.first()}",Labels.HIGHSCORE.skinKey){
            setPosition(-190f,30f)
        }
        label("    2 -                   ${scoreArray[1]}",Labels.HIGHSCORE.skinKey){
            setPosition(-190f,5f)
        }
        label("    3 -                   ${scoreArray[2]}",Labels.HIGHSCORE.skinKey){
            setPosition(-190f,-20f)
        }
        label("    4 -                   ${scoreArray[3]}",Labels.HIGHSCORE.skinKey){
            setPosition(-190f,-45f)
        }
        label("    5 -                   ${scoreArray[4]}",Labels.HIGHSCORE.skinKey){
            setPosition(-190f,-70f)

        }
    }

}

fun <T> KWidget<T>.highScoreHud(
    init : HighScoreHud.(T) -> Unit = {}
) : HighScoreHud = actor(HighScoreHud(),init)

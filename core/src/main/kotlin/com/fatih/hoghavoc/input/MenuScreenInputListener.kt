package com.fatih.hoghavoc.input

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.fatih.hoghavoc.events.ScreenType
import com.fatih.hoghavoc.screen.MenuScreen
import ktx.app.KtxInputAdapter

class MenuScreenInputListener(private val menuScreen: MenuScreen) : KtxInputAdapter ,InputListener() {


    override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
        println(event.target.javaClass)
        return if(event.target.javaClass == Label::class.java){
            when((event.target as Label).text.toString()){

                " PLAY" ->{
                    menuScreen.screenListener.setScreen(ScreenType.GAME)
                }
                "SCORE" ->{
                    menuScreen.openHighScoreScreen()
                }
                "HOW TO PLAY" ->{
                    menuScreen.openHowToPlayScreen()
                }
            }

            true
        }else if(event.target.javaClass == Image::class.java){
            menuScreen.closeHtpScreen()
            true
        }

        else false
    }

}

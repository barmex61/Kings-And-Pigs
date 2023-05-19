package com.fatih.hoghavoc.screen

import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.events.ScreenListener
import com.fatih.hoghavoc.input.MenuScreenInputListener
import com.fatih.hoghavoc.screen.GameScreen.Companion.inputMultiplexer
import com.fatih.hoghavoc.ui.view.MenuView
import com.fatih.hoghavoc.ui.view.menuView
import com.fatih.hoghavoc.ui.widget.HowToPlayHud
import com.fatih.hoghavoc.ui.widget.highScoreHud
import com.fatih.hoghavoc.ui.widget.howToPlayHud
import ktx.app.KtxScreen
import ktx.scene2d.actors

class MenuScreen(private val uiStage:Stage) : KtxScreen {

    private var menuView : MenuView
    private val menuScreenInputListener = MenuScreenInputListener(this)
    lateinit var screenListener: ScreenListener

    init {
        uiStage.actors {
           menuView = menuView().also {
               it.addActions()
           }
        }
        inputMultiplexer.run {
            addProcessor(uiStage)
        }
        menuView.addListeners(menuScreenInputListener)
    }

    fun openHowToPlayScreen(){
        menuView.removeActor(menuView.bannerHud)
        menuView.run {
            if (htpHud == null){
                htpHud =  howToPlayHud{
                    this.backButton.addListener(menuScreenInputListener)
                }
            }else{
                addActor(htpHud)
            }
        }
    }

    fun openHighScoreScreen(){
        menuView.removeActor(menuView.bannerHud)
        menuView.run {
            if (hsHud == null){
                hsHud = highScoreHud{
                    this.backButton.addListener(menuScreenInputListener)
                }
            }else{
                addActor(hsHud)
            }
        }
    }

    fun closeHtpScreen(){
        menuView.removeActor(menuView.htpHud)
        menuView.removeActor(menuView.hsHud)
        menuView.run {
            addActor(bannerHud)
        }
    }

    override fun show() {
    }

    override fun render(delta: Float) {
        uiStage.apply {
            act(delta)
            draw()
        }
    }


    override fun dispose() {
        println("dispose")
        uiStage.clear()
    }
}

package com.fatih.hoghavoc

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.fatih.hoghavoc.events.PauseEvent
import com.fatih.hoghavoc.events.ScreenListener
import com.fatih.hoghavoc.events.ScreenType
import com.fatih.hoghavoc.screen.GameScreen
import com.fatih.hoghavoc.screen.MenuScreen
import com.fatih.hoghavoc.ui.disposeSkin
import com.fatih.hoghavoc.ui.loadSkin
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely

class HogHavoc : KtxGame<KtxScreen>() , EventListener , ScreenListener{
    private val spriteBatch by lazy {
        SpriteBatch()
    }
    private val gameStage by lazy { Stage(ExtendViewport(16f,9f),spriteBatch) }
    private val uiStage by lazy { Stage(ExtendViewport(400f,225f),spriteBatch) }
    private var pause = false
    private var dt = 0f

    companion object{
        val preferences by lazy {
            Gdx.app.getPreferences("hog_havoc")
        }
    }

    override fun create() {
        loadSkin()
        gameStage.addListener(this)
        setScreen(ScreenType.MENU)
        Gdx.input.inputProcessor = GameScreen.inputMultiplexer

    }

    override fun resize(width: Int, height: Int) {
        gameStage.viewport.update(width,height,true)
        uiStage.viewport.update(width,height,true)
    }
    
    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        dt = if (pause) 0f else Gdx.graphics.deltaTime
        currentScreen.render(dt)
    }

    override fun handle(event: Event?): Boolean {
        return when(event){
            is PauseEvent -> {
                pause = event.pause
                if (event.pause) currentScreen.pause() else currentScreen.resume()
                true
            }

            else -> false
        }
    }

    override fun setScreen(screenType: ScreenType) {
        removeScreen((currentScreen as KtxScreen)::class.java)
        (currentScreen as KtxScreen).disposeSafely()
        when(screenType){
            ScreenType.MENU ->{
                addScreen(MenuScreen(uiStage).also { it.screenListener = this })
                setScreen(MenuScreen::class.java)
            }
            ScreenType.GAME->{
                addScreen(GameScreen(gameStage,uiStage).also { it.screenListener = this })
                setScreen(GameScreen::class.java)
            }
        }
    }

    override fun dispose() {
        disposeSkin()
        spriteBatch.disposeSafely()
        gameStage.disposeSafely()
        uiStage.disposeSafely()
    }

}

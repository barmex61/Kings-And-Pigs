package com.fatih.hoghavoc

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.fatih.hoghavoc.events.PauseEvent
import com.fatih.hoghavoc.screen.GameScreen
import com.fatih.hoghavoc.ui.disposeSkin
import com.fatih.hoghavoc.ui.loadSkin
import com.fatih.hoghavoc.utils.PerformanceProfiler
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely

class HogHavoc : KtxGame<KtxScreen>() , EventListener{
    private lateinit var performanceProfiler : PerformanceProfiler
    private val gameStage by lazy { Stage(ExtendViewport(16f,9f),performanceProfiler.spriteBatch) }
    private val uiStage by lazy { Stage(ExtendViewport(400f,225f),performanceProfiler.spriteBatch) }
    private var pause = false
    private var dt = 0f

    override fun create() {
        loadSkin()
        performanceProfiler = PerformanceProfiler().apply {
            this.create()
        }
        gameStage.addListener(this)
        addScreen(GameScreen(gameStage,uiStage))
        setScreen<GameScreen>()

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

    override fun dispose() {
        disposeSkin()
        performanceProfiler.dispose()
        //spriteBatch.disposeSafely()
        gameStage.disposeSafely()
        uiStage.disposeSafely()
    }

}

package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.TimeUtils
import com.fatih.hoghavoc.component.ImageComponent
import com.fatih.hoghavoc.events.MapChangeEvent
import com.fatih.hoghavoc.utils.UNIT_SCALE
import com.github.quillraven.fleks.*
import ktx.assets.disposeSafely
import ktx.collections.GdxArray
import ktx.graphics.use


class RenderSystem (
    private val gameStage : Stage,
    @Qualifier("uiStage") private val uiStage : Stage
    ): IntervalSystem(Fixed(1/300f)), EventListener {

    private val mapRenderer : OrthogonalTiledMapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE,SpriteBatch(1))
    private val gameCamera : OrthographicCamera = gameStage.camera as OrthographicCamera
    private val tiledMapTileLayer = GdxArray<TiledMapTileLayer>()

    init {
        mapRenderer.setView(gameCamera)
    }

    override fun onAlpha(alpha: Float) {
        gameStage.run{
            mapRenderer.batch.use {
                tiledMapTileLayer.forEach { layer->
                    mapRenderer.renderTileLayer(layer)
                }
            }
            draw()
        }
        uiStage.run {
            act(deltaTime)
            draw()
        }
        mapRenderer.setView(gameCamera)


    }



    override fun handle(event: Event?): Boolean {
        return when(event){
            is MapChangeEvent ->{
                event.map.layers.getByType(TiledMapTileLayer::class.java,tiledMapTileLayer)
                true
            }
            else -> false
        }
    }

    override fun onDispose() {
        mapRenderer.disposeSafely()
    }

    override fun onTick() {

    }

}

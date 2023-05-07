package com.fatih.hoghavoc.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.MapChangeEvent
import com.fatih.hoghavoc.input.KeyboardInputProcessor
import com.fatih.hoghavoc.system.*
import com.fatih.hoghavoc.ui.model.GameModel
import com.fatih.hoghavoc.ui.view.PauseView
import com.fatih.hoghavoc.ui.view.gameView
import com.fatih.hoghavoc.ui.view.pauseView
import com.fatih.hoghavoc.utils.fireEvent
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.scene2d.actors

class GameScreen(private val gameStage:Stage,private val uiStage: Stage) : KtxScreen{


    private val textureAtlas : TextureAtlas = TextureAtlas("gameObjects/gameObjects.atlas")
    private val tmxMapLoader : TmxMapLoader = TmxMapLoader()
    private var gameModel : GameModel
    private val currentMap : TiledMap = tmxMapLoader.load("mapObjects/map1.tmx")
    private val physicWorld  = createWorld(Vector2(0f,-9.8f),false)
    private var pause : Boolean = false
    private val world : World = world{
        injectables {
            add("uiStage",uiStage)
            add(gameStage)
            add(textureAtlas)
            add(physicWorld)
        }

        components {
            add<FloatingTextComponent.Companion.FloatingTextComponentListener>()
            add<ImageComponent.Companion.ImageComponentListener>()
            add<PlayerStateComponent.Companion.StateComponentListener>()
            add<AiComponent.Companion.AiComponentListener>()
            add<AttackFixtureComponent.Companion.AttackFixtureComponentListener>()
        }

        systems {
            add<EntitySpawnSystem>()
            add<AnimationSystem>()
            add<MoveSystem>()
            add<PhysicSystem>()
            add<AttackSystem>()
            add<AttackFixtureSystem>()
            add<LifeSystem>()
            add<DeadSystem>()
            add<FloatingTextSystem>()
            add<StateSystem>()
            add<AiSystem>()
            add<AudioSystem>()
            add<CameraSystem>()
            add<RenderSystem>()
            add<DebugSystem>()
        }

    }

    init {
        gameModel = GameModel(world)
        uiStage.actors {
            gameView(gameModel)
            pauseView {
                this.isVisible = false
            }
        }
        gameStage.addListener(gameModel)
    }

    override fun show() {
        world.systems.forEach {system->
            if (system is EventListener){
                gameStage.addListener(system)
            }
        }
        gameStage.fireEvent(MapChangeEvent(currentMap))
        KeyboardInputProcessor(world, gameStage = gameStage)
    }

    override fun pause() = pauseWorld(true)

    override fun resume() = pauseWorld(false)

    override fun render(delta: Float) {
        val dt = delta.coerceAtMost(0.25f)
        GdxAI.getTimepiece().update(dt)
        world.update(dt)
    }

    private fun pauseWorld(pause:Boolean){
        val mandatorySystems = setOf(
            AnimationSystem::class,
            CameraSystem::class,
            RenderSystem::class,
            DebugSystem::class
        )
        world.systems.filter {
            it::class !in mandatorySystems
        }.forEach {
            it.enabled = !pause
        }

        uiStage.actors.filterIsInstance<PauseView>().first().isVisible = pause
    }

    override fun dispose() {
        textureAtlas.disposeSafely()
        currentMap.disposeSafely()
        uiStage.disposeSafely()
        gameStage.disposeSafely()
        physicWorld.disposeSafely()
        Gdx.audio.newMusic(Gdx.files.internal("Audio/mapmusic.mp3")).stop()
        world.dispose()
    }
}

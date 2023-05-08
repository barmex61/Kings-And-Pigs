package com.fatih.hoghavoc.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.MapChangeEvent
import com.fatih.hoghavoc.input.KeyboardInputProcessor
import com.fatih.hoghavoc.system.*
import com.fatih.hoghavoc.ui.Drawables
import com.fatih.hoghavoc.ui.model.GameModel
import com.fatih.hoghavoc.ui.view.PauseView
import com.fatih.hoghavoc.ui.view.gameView
import com.fatih.hoghavoc.ui.view.pauseView
import com.fatih.hoghavoc.utils.fireEvent
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import ktx.actors.alpha
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import com.fatih.hoghavoc.ui.get


class GameScreen(private val gameStage:Stage,private val uiStage: Stage) : KtxScreen {


    private val textureAtlas : TextureAtlas = TextureAtlas("gameObjects/gameObjects.atlas")
    private val tmxMapLoader : TmxMapLoader = TmxMapLoader()
    private var gameModel : GameModel
    private val currentMap : TiledMap = tmxMapLoader.load("mapObjects/map1.tmx")
    private val physicWorld  = createWorld(Vector2(0f,-9.8f),false)
    private var keyboardInputProcessor: KeyboardInputProcessor
    private val touchPad : Touchpad = Touchpad(0.3f, Scene2DSkin.defaultSkin).apply {
        this.setPosition(1f,1f)
        this.setSize(2.5f,2.5f)
        this.alpha = 0.25f
    }
    private val attackImage : Image = Image(Scene2DSkin.defaultSkin[Drawables.ATTACK]).apply {
        setPosition(5f,4f)
        setSize(1.5f,2f)
        alpha = 0.7f
    }
    private val world : World = world{
        injectables {
            add("uiStage",uiStage)
            add(gameStage)
            add(textureAtlas)
            add(physicWorld)
            add(touchPad)
            add(attackImage)
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
        gameStage.run {
            addActor(touchPad.also {
                it.toFront()
            })
            addActor(attackImage.also {
                it.toFront()
            })
            addListener(gameModel)
            inputMultiplexer.addProcessor(gameStage)
        }
        keyboardInputProcessor = KeyboardInputProcessor(world, gameStage = gameStage){actor,setAlpha->
            when (actor) {
                "touchpad" -> {
                    touchPad.alpha = if (setAlpha) 0.7f else 0.25f
                }
                "attack" -> {
                    attackImage.alpha = if (setAlpha) 1f else 0.7f
                }
                else -> {
                    touchPad.alpha = 0.25f
                    attackImage.alpha = 0.7f
                }
            }
        }
        touchPad.addListener(keyboardInputProcessor)
        attackImage.addListener(keyboardInputProcessor)

    }

    override fun show() {
        world.systems.forEach {system->
            if (system is EventListener){
                gameStage.addListener(system)
            }
        }
        gameStage.fireEvent(MapChangeEvent(currentMap))

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

    companion object{
        val inputMultiplexer = InputMultiplexer()
    }
}

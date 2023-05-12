package com.fatih.hoghavoc.screen

import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.utils.Array
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.CurrentMapChangeEvent
import com.fatih.hoghavoc.events.DestroyWorld
import com.fatih.hoghavoc.events.MapChangeEvent
import com.fatih.hoghavoc.events.StartStageActionEvent
import com.fatih.hoghavoc.input.KeyboardInputProcessor
import com.fatih.hoghavoc.system.*
import com.fatih.hoghavoc.ui.model.GameModel
import com.fatih.hoghavoc.ui.view.GameView
import com.fatih.hoghavoc.ui.view.PauseView
import com.fatih.hoghavoc.ui.view.gameView
import com.fatih.hoghavoc.ui.view.pauseView
import com.fatih.hoghavoc.utils.NOTHING_BIT
import com.fatih.hoghavoc.utils.fireEvent
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import ktx.actors.alpha
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.collections.isNotEmpty
import ktx.scene2d.actors


class GameScreen(private val gameStage:Stage,private val uiStage: Stage) : KtxScreen , EventListener{


    private val textureAtlas : TextureAtlas = TextureAtlas("gameObjects/gameObjects.atlas")
    private val tmxMapLoader : TmxMapLoader = TmxMapLoader()
    private var gameModel : GameModel
    private var currentMap : TiledMap = tmxMapLoader.load("mapObjects/map1.tmx")
    private var currentMapPath = "mapObjects/map1.tmx"
    private val physicWorld  = createWorld(Vector2(0f,-9.8f),false)
    private var keyboardInputProcessor: KeyboardInputProcessor
    private var gameView: GameView
    private val isMobile = Gdx.app.type == ApplicationType.Android || Gdx.app.type == ApplicationType.iOS
    private val bodies : Array<Body> = Array<Body>()
    private var pauseView : PauseView

    private var world : World = world{
        injectables {
            add("uiStage",uiStage)
            add(gameStage)
            add(textureAtlas)
            add(physicWorld)
            add(isMobile)
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
            add<RenderSystem>()
            add<CameraSystem>()
            add<DebugSystem>()
        }

    }

    init {

        gameModel = GameModel(world)
        uiStage.actors {
            gameView = gameView(( Gdx.app.type == ApplicationType.Android || Gdx.app.type == ApplicationType.iOS),gameModel)
            pauseView = pauseView {
                this.isVisible = false
            }
            inputMultiplexer.addProcessor(uiStage)
        }
        gameStage.run {
            addListener(gameModel)
            addListener(this@GameScreen)
        }
        keyboardInputProcessor = KeyboardInputProcessor(world, gameStage = gameStage, alphaChangeLambda = gameView.changeAlphaLambda)
        gameView.addInputListener(keyboardInputProcessor)

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

        pauseView.isVisible = pause
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

    override fun handle(event: Event): Boolean {
        return when(event){

            is StartStageActionEvent -> {
                pauseView.run {
                    pauseLabel.setText("")
                    isVisible = true
                    alpha = 0f
                    clearActions()
                    addAction(fadeIn(2f, Interpolation.smooth2))
                }
                true
            }

            is CurrentMapChangeEvent->{
                if (currentMapPath != event.mapStr){
                    currentMap.disposeSafely()
                    pauseView.run {
                        isVisible = true
                        clearActions()
                        addAction(Actions.fadeOut(2f, Interpolation.smooth2))
                    }
                    world.system<PhysicSystem>().destroyWorld = true
                    currentMap = tmxMapLoader.load(event.mapStr)
                    currentMapPath = event.mapStr
                }

                true
            }
            is DestroyWorld ->{
                physicWorld.getBodies(bodies)
                synchronized(physicWorld){
                    for (i in 0 until bodies.size){
                        physicWorld.destroyBody(bodies[i])
                    }
                }
                bodies.clear()
                world.system<AttackSystem>().playerBodyPos = null
                world.family(noneOf = arrayOf(TiledComponent::class)).forEach {
                    world.remove(it)
                }
                gameStage.fireEvent(MapChangeEvent(currentMap))
                true
            }
            else ->false
        }
    }

    companion object{
        val inputMultiplexer = InputMultiplexer()
    }
}

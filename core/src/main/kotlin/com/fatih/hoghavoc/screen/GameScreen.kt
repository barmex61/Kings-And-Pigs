package com.fatih.hoghavoc.screen

import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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
import com.fatih.hoghavoc.events.*
import com.fatih.hoghavoc.input.KeyboardInputProcessor
import com.fatih.hoghavoc.system.*
import com.fatih.hoghavoc.ui.model.GameModel
import com.fatih.hoghavoc.ui.view.GameView
import com.fatih.hoghavoc.ui.view.PauseView
import com.fatih.hoghavoc.ui.view.gameView
import com.fatih.hoghavoc.ui.view.pauseView
import com.fatih.hoghavoc.utils.AudioManager
import com.fatih.hoghavoc.utils.fireEvent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import ktx.actors.alpha
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.scene2d.actors


class GameScreen(private val gameStage:Stage,private val uiStage: Stage) : KtxScreen , EventListener{


    private val textureAtlas : TextureAtlas = TextureAtlas("gameObjects/gameObjects.atlas")
    private val tmxMapLoader : TmxMapLoader = TmxMapLoader()
    private lateinit var gameModel : GameModel
    private var currentMap : TiledMap = tmxMapLoader.load("mapObjects/map1.tmx")
    private var currentMapPath = "mapObjects/map1.tmx"
    private val physicWorld  = createWorld(Vector2(0f,-9.8f),false)
    private var keyboardInputProcessor: KeyboardInputProcessor
    private var gameView: GameView
    private val isMobile = Gdx.app.type == ApplicationType.Android || Gdx.app.type == ApplicationType.iOS
    private val bodies : Array<Body> = Array<Body>()
    private var pauseView : PauseView
    private var createStages = true
    lateinit var screenListener: ScreenListener

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
            add<DialogComponent.Companion.DialogComponentListener>()
        }

        systems {
            add<EntitySpawnSystem>()
            add<AnimationSystem>()
            add<DialogSystem>()
            add<DestructableItemsSystem>()
            add<ItemSystem>()
            add<MoveSystem>()
            add<PhysicSystem>()
            add<AttackSystem>()
            add<AttackFixtureSystem>()
            add<LifeSystem>()
            add<DeadSystem>()
            add<FloatingTextSystem>()
            add<StateSystem>()
            add<AiSystem>()
            add<RenderSystem>()
            add<CameraSystem>()
            add<DebugSystem>()
        }

    }

    init {
        gameModel = GameModel(world = world)
        uiStage.actors {
            gameView = gameView(( Gdx.app.type == ApplicationType.Android || Gdx.app.type == ApplicationType.iOS),gameModel)
            pauseView = pauseView {
                this.isVisible = false
            }
            inputMultiplexer.addProcessor(uiStage)
        }
        gameStage.run {
            addListener(gameModel)
            addListener(AudioManager)
            addListener(this@GameScreen)
        }
        keyboardInputProcessor = KeyboardInputProcessor(world, gameStage = gameStage, alphaChangeLambda = gameView.changeAlphaLambda)
        gameView.addInputListener(keyboardInputProcessor)
    }

    override fun show() {
        createStages = false
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)){
            gameStage.fireEvent(CurrentMapChangeEvent("mapObjects/map1.tmx",true))
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)){
            screenListener.setScreen(ScreenType.MENU)
        }
        println(gameStage.actors.size)
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
        AudioManager.music?.stop()
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
                if (currentMapPath != event.mapStr || event.restart){
                    currentMap.disposeSafely()
                    if (!event.restart){
                        pauseView.run {
                            isVisible = true
                            clearActions()
                            addAction(Actions.fadeOut(2f, Interpolation.smooth2))
                        }
                    }
                    currentMap = tmxMapLoader.load(event.mapStr)
                    currentMapPath = event.mapStr
                    physicWorld.getBodies(bodies)
                    var entity : Entity?=null
                    world.family(allOf = arrayOf(PlayerComponent::class)).forEach {
                        entity = it
                    }
                    synchronized(physicWorld){
                        for (i in 0 until bodies.size){
                            physicWorld.destroyBody(bodies[i])
                        }
                    }
                    bodies.clear()
                    world.system<AttackSystem>().playerBodyPos = null
                    world.family(noneOf = arrayOf(PlayerComponent::class)).forEach {
                        world.remove(it)
                    }
                    gameStage.fireEvent(MapChangeEvent(currentMap,entity))
                }

                true
            }
            is DestroyWorld ->{

                true
            }
            else ->false
        }
    }

    companion object{
        val inputMultiplexer = InputMultiplexer()
    }
}

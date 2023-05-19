package com.fatih.hoghavoc.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.PauseEvent
import com.fatih.hoghavoc.events.RespawnPlayer
import com.fatih.hoghavoc.screen.GameScreen.Companion.inputMultiplexer
import com.fatih.hoghavoc.utils.fireEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.app.KtxInputAdapter

class KeyboardInputProcessor(
    world: World,
    private val moveComps : ComponentMapper<MoveComponent> = world.mapper(),
    private val attackComps : ComponentMapper<AttackComponent> = world.mapper(),
    private val gameStage : Stage,
    private val alphaChangeLambda : (String,Boolean) -> Unit
) : KtxInputAdapter , InputListener() {

    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))
    private var playerSin = 0f
    private var playerCos = 0f
    private var playerAttack : Boolean = false
    private var paused = false

    init {
        inputMultiplexer.addProcessor(this)
    }

    private fun Int.isGameKey() = this == Input.Keys.UP || this == Input.Keys.RIGHT || this == Input.Keys.LEFT || this == Input.Keys.SHIFT_LEFT || this == Input.Keys.SPACE

    private fun updatePlayerMovement(isDragged : Boolean = false,isAttack : Boolean = false){
        if (isDragged){
            playerEntities.forEach {player->
                moveComps[player].run {
                    cos = playerCos
                    sin = playerSin
                }
            }
        }
        if (isAttack){
            playerEntities.forEach {player->
            attackComps[player].run {
                if (playerAttack){
                    playerAttack = false
                  }
               }
            }
        }
        if (!isAttack && !isDragged){
            playerEntities.forEach {player->
                moveComps[player].run {
                    cos = playerCos
                    sin = playerSin
                }
                attackComps[player].run {
                    if (playerAttack){
                        doAttack = true
                        playerAttack = false
                    }
                }
            }
        }

    }

    override fun keyDown(keycode: Int): Boolean {
        return if (keycode.isGameKey()){
            when(keycode){
                Input.Keys.UP ->{if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                    playerSin = 1f
                } }
                Input.Keys.LEFT ->{playerCos = -1f}
                Input.Keys.RIGHT ->{playerCos = 1f}
                Input.Keys.SPACE -> {
                    playerAttack = true
                }
            }
            updatePlayerMovement()
            true
        }else if (keycode == Input.Keys.P){
            paused = !paused
            gameStage.fireEvent(PauseEvent(paused))
            true
        }
        else false
    }


    override fun keyUp(keycode: Int): Boolean {
        return if (keycode.isGameKey()){
            when(keycode){
                Input.Keys.UP ->{playerSin = 0f}
                Input.Keys.LEFT ->{playerCos = if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) 1f else 0f}
                Input.Keys.RIGHT ->{playerCos = if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) -1f else 0f}
            }
            updatePlayerMovement()
            true
        }
        else false
    }

    override fun touchDown(
        event: InputEvent?,
        x: Float,
        y: Float,
        pointer: Int,
        button: Int
    ): Boolean {
        if (event!!.target.javaClass == Touchpad::class.java){
            alphaChangeLambda("touchpad",true)
        }else{
            playerAttack = true
            updatePlayerMovement(isDragged = false,isAttack = true)
            alphaChangeLambda("attack",true)
            if (playerEntities.isEmpty){
                gameStage.fireEvent(RespawnPlayer())
            }
        }
        return true
    }

    override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
        if (event!!.target.javaClass == Touchpad::class.java){
            playerCos = scaleValue(x,false)
            playerSin = scaleValue(y,true)
            updatePlayerMovement(isDragged = true,isAttack = false)
        }

    }

    private fun scaleValue(value: Float,sin:Boolean): Float {
        val clampedValue = value.coerceIn(0.6f, 2.5f)
        return if (sin) {
            if (clampedValue == 2.5f) 1f else 0f
        } else {
            2f * (clampedValue - 0.6f) / (2.5f - 0.6f) - 1f
        }
    }

    override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
        if (event!!.target.javaClass == Touchpad::class.java){
            playerCos = 0f
            playerSin = 0f
            updatePlayerMovement(true)
        }else{
            playerAttack = false
        }
        alphaChangeLambda("",false)
    }

}

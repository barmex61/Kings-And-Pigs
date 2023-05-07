package com.fatih.hoghavoc.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.component.AttackComponent
import com.fatih.hoghavoc.component.AttackState
import com.fatih.hoghavoc.component.MoveComponent
import com.fatih.hoghavoc.component.PlayerComponent
import com.fatih.hoghavoc.events.PauseEvent
import com.fatih.hoghavoc.utils.fireEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.app.KtxInputAdapter

class KeyboardInputProcessor(
    world: World,
    private val moveComps : ComponentMapper<MoveComponent> = world.mapper(),
    private val attackComps : ComponentMapper<AttackComponent> = world.mapper(),
    private val gameStage : Stage
) : KtxInputAdapter {

    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))
    private var playerSin = 0f
    private var playerCos = 0f
    private var playerAttack : Boolean = false
    private var paused = false

    init {
        Gdx.input.inputProcessor = this
    }

    private fun Int.isGameKey() = this == Input.Keys.UP || this == Input.Keys.RIGHT || this == Input.Keys.LEFT || this == Input.Keys.SHIFT_LEFT || this == Input.Keys.SPACE

    private fun updatePlayerMovement(){
        playerEntities.forEach {player->
            moveComps[player].run {
                cos = playerCos
                sin = playerSin
            }
            attackComps[player].run {
                if (playerAttack){
                    attackState = AttackState.ATTACK
                    playerAttack = false
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
}

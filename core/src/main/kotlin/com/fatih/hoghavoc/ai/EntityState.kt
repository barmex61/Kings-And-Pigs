package com.fatih.hoghavoc.ai

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram

interface EntityState : State<PlayerAiEntity> {
    override fun enter(entity:PlayerAiEntity) = Unit
    override fun update(entity: PlayerAiEntity) = Unit
    override fun onMessage(entity: PlayerAiEntity, telegram: Telegram?) = false
    override fun exit(entity: PlayerAiEntity) = Unit
}

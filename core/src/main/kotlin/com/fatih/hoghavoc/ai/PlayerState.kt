package com.fatih.hoghavoc.ai

import com.badlogic.gdx.graphics.g2d.Animation
import com.fatih.hoghavoc.component.AnimationType
import com.fatih.hoghavoc.component.TextureType
import com.fatih.hoghavoc.utils.DEFAULT_FRAME_DURATION

sealed class PlayerState : EntityState {

    object DOOR_OUT : PlayerState(){
        override fun enter(entity: PlayerAiEntity) {
            entity.startAnimation(AnimationType.DOOR_OUT,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION * 1.5f)
            entity.root(true)
        }

        override fun update(entity: PlayerAiEntity) {
            if (entity.animationComponent.isAnimationDone(AnimationType.DOOR_OUT)){
                entity.changeState(IDLE)
            }
        }
    }

    object DOOR_IN : PlayerState(){
        override fun enter(entity: PlayerAiEntity) {
            entity.root(true)
            entity.startAnimation(AnimationType.DOOR_IN,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION * 1.5f)
        }

        override fun update(entity: PlayerAiEntity) {
            if (entity.animationComponent.isAnimationDone(AnimationType.DOOR_IN)){
                entity.changeState(DOOR_OUT)
            }
        }
    }

    object IDLE : PlayerState(){
        override fun enter(entity: PlayerAiEntity) {
            entity.root(false)
            entity.startAnimation(AnimationType.IDLE,Animation.PlayMode.LOOP)
        }

        override fun update(entity: PlayerAiEntity) {
            when{
                entity.isDead -> entity.changeState(DEAD)
                entity.getHit -> entity.changeState(HIT)
                entity.isRunning -> entity.changeState(RUN)
                entity.wantsToAttack -> entity.changeState(ATTACK)
                entity.isJumping-> entity.changeState(JUMP)
                entity.isFalling -> entity.changeState(FALL)
            }
        }
    }

    object RUN : PlayerState(){
        override fun enter(entity: PlayerAiEntity) {
            entity.startAnimation(AnimationType.RUN,Animation.PlayMode.LOOP)
        }

        override fun update(entity: PlayerAiEntity) {
            when{
                entity.isDead -> entity.changeState(DEAD)
                entity.getHit -> entity.changeState(HIT)
                !entity.isRunning -> entity.changeState(IDLE)
                entity.wantsToAttack -> entity.changeState(ATTACK)
                entity.isJumping-> entity.changeState(JUMP)
                entity.isFalling -> entity.changeState(FALL)
            }
        }
    }

    object JUMP : PlayerState(){
        override fun enter(entity: PlayerAiEntity) {
            entity.setTexture(TextureType.JUMP)
        }

        override fun update(entity: PlayerAiEntity) {
            when{
                entity.isDead -> entity.changeState(DEAD)
                entity.getHit -> entity.changeState(HIT)
                entity.wantsToAttack -> entity.changeState(ATTACK)
                entity.isFalling -> entity.changeState(FALL)
                !entity.isFalling && !entity.isJumping -> entity.changeState(IDLE)
            }
        }
    }

    object FALL : PlayerState(){
        override fun enter(entity: PlayerAiEntity) {
            entity.setTexture(TextureType.FALL)
        }

        override fun update(entity: PlayerAiEntity) {
            when{
                entity.isDead -> entity.changeState(DEAD)
                entity.getHit -> entity.changeState(HIT)
                entity.wantsToAttack -> entity.changeState(ATTACK)
                !entity.isFalling && !entity.isJumping -> entity.changeState(IDLE)
            }
        }
    }

    object ATTACK : PlayerState(){
        override fun enter(entity: PlayerAiEntity) {
            entity.startAnimation(AnimationType.ATTACK,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION * 1.5f)
        }

        override fun update(entity: PlayerAiEntity) {
            when{
                entity.isDead -> entity.changeState(DEAD)
                entity.getHit -> entity.changeState(HIT)
                entity.isRunning -> entity.changeState(RUN)
                !entity.wantsToAttack -> entity.changeState(IDLE,true)
            }
        }
    }

    object HIT : PlayerState(){
        private var timer = 150L
        override fun enter(entity: PlayerAiEntity) {
            entity.root(true)
        }

        override fun update(entity: PlayerAiEntity) {
            timer -= 10L
            if (timer<=0L){
                timer = 150L
                entity.changeState(IDLE)
            }
        }
    }

    object DEAD : PlayerState(){
        override fun enter(entity: PlayerAiEntity) {
            entity.root(true)
        }

        override fun update(entity: PlayerAiEntity) {
            when{
                entity.wantsToResurrect -> entity.changeState(RESURRECT)
            }
        }
    }


    object RESURRECT: PlayerState(){
        override fun enter(entity: PlayerAiEntity) {
            entity.startAnimation(AnimationType.DEAD,Animation.PlayMode.REVERSED, DEFAULT_FRAME_DURATION * 2f)
            entity.root(true)
        }

        override fun update(entity: PlayerAiEntity) {
            if (entity.animationComponent.isAnimationDone(AnimationType.DEAD,Animation.PlayMode.REVERSED)){
                entity.root(false)
                entity.changeState(IDLE)
            }
        }
    }
}

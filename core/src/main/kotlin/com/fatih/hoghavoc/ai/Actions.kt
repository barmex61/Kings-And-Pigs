package com.fatih.hoghavoc.ai

import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute
import com.badlogic.gdx.ai.utils.random.FloatDistribution
import com.badlogic.gdx.graphics.g2d.Animation
import com.fatih.hoghavoc.component.AnimationType
import com.fatih.hoghavoc.component.TextureType
import com.fatih.hoghavoc.events.EnemyDamageEvent
import com.fatih.hoghavoc.utils.DEFAULT_FRAME_DURATION
import com.fatih.hoghavoc.utils.fireEvent
import ktx.math.random
import ktx.math.vec2

abstract class Action : LeafTask<EnemyAiEntity>(){
    val entity : EnemyAiEntity
        get() = `object`
    protected var deltaTime :Float = 0f
    override fun copyTo(task: Task<EnemyAiEntity>) = task

}

class IdleTask(
    @JvmField
    @TaskAttribute(required = true)
    var duration : FloatDistribution? = null
) : Action(){
    private var currentDuration = 0f

    override fun execute(): Status {
        if (entity.entity.id == 43){
            println("idle")
        }
        deltaTime = GdxAI.getTimepiece().deltaTime
        if (status != Status.RUNNING){

            entity.stopMovement()
            entity.startAnimation(AnimationType.IDLE,Animation.PlayMode.LOOP, DEFAULT_FRAME_DURATION)
            currentDuration = duration?.nextFloat() ?: 1f
            return Status.RUNNING
        }
        currentDuration -= deltaTime
        if (currentDuration <= 0f){
            return Status.SUCCEEDED
        }
        if (entity.isGetHit()){
            return Status.FAILED
        }

        return Status.RUNNING
    }

    override fun copyTo(task: Task<EnemyAiEntity>): Task<EnemyAiEntity> {
        (task as IdleTask).duration = duration
        return task
    }
}

class WanderTask : Action(){

    private val startLocation = vec2()
    private val targetLocation = vec2()
    private var timer = 2f

    override fun execute(): Status {
        deltaTime = GdxAI.getTimepiece().deltaTime
        timer -= deltaTime
        if (entity.entity.id == 43){
            println("wander")
        }
        if (status != Status.RUNNING){

            entity.startAnimation(AnimationType.RUN,Animation.PlayMode.LOOP, DEFAULT_FRAME_DURATION * 2f)
            if (startLocation.isZero){
                startLocation.set(entity.location)
            }
            targetLocation.set(
                startLocation.x +(-entity.moveRange..entity.moveRange).random(),
                startLocation.y
            )

            entity.moveTo(targetLocation)
            return Status.RUNNING
        }
        when{
            entity.inRange(0.8f,targetLocation)->{
                entity.stopMovement()

                return Status.SUCCEEDED
            }
            timer <= 0f ->{
                timer = 2f
                return Status.FAILED
            }
            entity.hasEnemyNearby() -> {
                return Status.FAILED
            }
            entity.isGetHit() ->{
                return Status.FAILED
            }
        }
        return Status.RUNNING
    }
}

class MeleeAttackTask : Action(){
    override fun execute(): Status {

        deltaTime = GdxAI.getTimepiece().deltaTime
        if (entity.entity.id == 43){
            println("melee")
        }
        if (!entity.isAlive()){
            return Status.FAILED
        }
        if (status != Status.RUNNING){
            entity.faceToKing()
            entity.gameStage.fireEvent(EnemyDamageEvent(entity.entity))
            entity.startAnimation(AnimationType.ATTACK, Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION )
            entity.stopMovement()
            entity.startAttack()
            return Status.RUNNING
        }
        if (entity.animationComponent.isAnimationDone(AnimationType.ATTACK,Animation.PlayMode.NORMAL)){
            entity.startAnimation(AnimationType.IDLE,Animation.PlayMode.LOOP, DEFAULT_FRAME_DURATION)
            return Status.SUCCEEDED
        }
        if (entity.isGetHit()){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}
class RangeAttackTask : Action(){
    override fun execute(): Status {
        deltaTime = GdxAI.getTimepiece().deltaTime
        if (entity.entity.id == 43){
            println("rangeattack")
        }
        if (!entity.isAlive()){
            return Status.FAILED
        }
        if (status != Status.RUNNING){
            entity.faceToKing()
            entity.gameStage.fireEvent(EnemyDamageEvent(entity.entity))
            entity.startAnimation(AnimationType.ATTACK, Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION )
            entity.stopMovement()
            entity.startAttack()
            println("startattack")
            return Status.RUNNING
        }
        if (entity.animationComponent.isAnimationDone(AnimationType.ATTACK,Animation.PlayMode.NORMAL)){
            entity.startAnimation(AnimationType.IDLE,Animation.PlayMode.LOOP, DEFAULT_FRAME_DURATION)
            return Status.SUCCEEDED
        }
        if (entity.isGetHit()){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}

class FallTask : Action(){
    override fun execute(): Status {
        if (entity.entity.id == 43){
            println("fall")
        }
        if(status != Status.RUNNING){

            entity.startTexture(TextureType.FALL)
            return Status.RUNNING
        }
        if (!entity.isFalling()){
            return Status.SUCCEEDED
        }
        if (entity.isJumping()){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}

class JumpTask : Action(){
    override fun execute(): Status {
        if (entity.entity.id == 43){
            println("jump")
        }
        if(status != Status.RUNNING){

            entity.startTexture(TextureType.JUMP)
            return Status.RUNNING
        }
        if (!entity.isJumping()){
            return Status.SUCCEEDED
        }
        if (entity.isFalling()){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}

class HitTask() : Action(){
    override fun execute(): Status {
        if (entity.entity.id == 43){
            println("hittask")
        }
        if (status != Status.RUNNING){
            entity.stopMovement()
            return Status.RUNNING
        }
        if (!entity.isGetHit()){
            return Status.FAILED
        }

        return Status.RUNNING
    }
}

class FocusTask : Action(){
    private var timer : Float = 0f
    private var timeCount : Float = 1f
    override fun execute(): Status {
        timer += GdxAI.getTimepiece().deltaTime
        if (entity.entity.id == 43){
            println("focus")
        }
        if (timer>timeCount){
            entity.moveTo(entity.playerTarget,true)
            timeCount += 1f
        }
        if (status != Status.RUNNING){

            entity.startAnimation(AnimationType.RUN,Animation.PlayMode.LOOP, DEFAULT_FRAME_DURATION / 0.7f)
            entity.moveTo(entity.playerTarget,true)
            return Status.RUNNING
        }
        if (!entity.hasEnemyNearby()){
            return Status.FAILED
        }
        if (entity.canAttack()){
            timer = 0f
            timeCount = 1f
            return Status.SUCCEEDED
        }
        if (entity.isGetHit()){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}


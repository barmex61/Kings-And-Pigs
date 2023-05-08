package com.fatih.hoghavoc.ai

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.TimeUtils
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.utils.DEFAULT_FRAME_DURATION
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2
import kotlin.math.abs
import kotlin.math.pow

class EnemyAiEntity(
    private val world: World,
    val entity: Entity,
    val gameStage: Stage,
    animComps : ComponentMapper<AnimationComponent> = world.mapper(),
    private val physicComps : ComponentMapper<PhysicComponent> = world.mapper(),
    moveComps : ComponentMapper<MoveComponent> = world.mapper(),
    aiComps : ComponentMapper<AiComponent> = world.mapper(),
    imageComps : ComponentMapper<ImageComponent> = world.mapper(),
    attackComps : ComponentMapper<AttackComponent> = world.mapper(),
    private val lifeComps : ComponentMapper<LifeComponent> = world.mapper()
){

    val animationComponent = animComps[entity]
    private val physicComponent = physicComps[entity]
    private val moveComponent : MoveComponent? = moveComps.getOrNull(entity)
    private val attackComponent = attackComps[entity]
    private val imageComponent = imageComps[entity]
    private val lifeComponent = lifeComps[entity]
    private val aiComponent = aiComps[entity]
    private val bodyPos = vec2()
    private val movingBodyPos : Vector2 = Vector2(physicComponent.body.position.x,physicComponent.body.position.y)
    private var xDiff = 0f
    private var yDiff = 0f
    private var distanceBetweenVectors = 0f
    private var distanceBetweenItself = 0f
    private var playerPhysicComponent : PhysicComponent?= null
    private var playerEntity : Entity? = null
    private var time = TimeUtils.millis()

    val playerTarget : Vector2 = vec2()

    val moveRange: Float
        get() = moveComponent?.moveRange ?: 0f

    val location : Vector2
        get() = physicComponent.body.position

    fun startAnimation(animationType: AnimationType, playMode: Animation.PlayMode, frameDuration : Float = DEFAULT_FRAME_DURATION ) {
        animationComponent.nextAnimation(animationType,playMode,frameDuration)
    }


    fun moveTo(targetLocation: Vector2,fromFocus : Boolean = false) {
        val (targetX,targetY) = targetLocation
        val (sourceX,sourceY) = physicComponent.body.position
        moveComponent?.run {
            val angleRad = MathUtils.atan2(targetY-sourceY,targetX-sourceX)
            cos = MathUtils.cos(angleRad)
            if (fromFocus && targetY > sourceY + 0.2f){
                (MathUtils.cos(angleRad)*20f).coerceAtMost(1f)
            }
        }
    }

    fun inRange(range: Float, targetLocation: Vector2): Boolean {
        time = TimeUtils.millis()
        moveComponent?.let {
            if (moveComponent.sin > 0f){
                moveComponent.sin  -= 0.2f
            }else{
                moveComponent.sin = 0f
            }
            bodyPos.set(physicComponent.body.position)
            xDiff = targetLocation.x - bodyPos.x
            yDiff = targetLocation.y - bodyPos.y
            distanceBetweenVectors = kotlin.math.sqrt(xDiff.pow(2) + yDiff.pow(2))

            return if (distanceBetweenVectors <= range * range ) {
                movingBodyPos.set(
                    physicComponent.body.position.x,
                    physicComponent.body.position.y
                )
                true
            }else{
                if (targetLocation.x > movingBodyPos.x - 0.2f){
                    moveComponent.cos = abs(moveComponent.cos)
                }else if (targetLocation.x < movingBodyPos.x - 0.7f){
                    moveComponent.cos = -abs(moveComponent.cos)
                }

                xDiff = physicComponent.body.position.x - movingBodyPos.x
                yDiff = physicComponent.body.position.y - movingBodyPos.y
                distanceBetweenItself = kotlin.math.sqrt(xDiff.pow(2)+yDiff.pow(2))
                if (distanceBetweenItself <= 0.00008f){
                    if (moveComponent.sin<=0f && time - moveComponent.timeBetweenJumps >= 1500L ){
                        moveComponent.sin = 1f
                        moveComponent.timeBetweenJumps = TimeUtils.millis()
                    }
                }
                movingBodyPos.set(
                    physicComponent.body.position.x,
                    physicComponent.body.position.y
                )
                false
            }
        }
        return false
    }

    fun stopMovement() {
        moveComponent?.run {
            cos = 0f
            sin = 0f
        }
    }

    fun canAttack(): Boolean {
        if (attackComponent.isAttacking){
            return false
        }
        playerEntity = aiComponent.nearbyEntities.firstOrNull()
        playerEntity?.let {player->
            if (playerPhysicComponent == null)
                playerPhysicComponent = physicComps[player]
            playerTarget.set(
                playerPhysicComponent!!.body.position.x + playerPhysicComponent!!.offset.x ,
                playerPhysicComponent!!.body.position.y + playerPhysicComponent!!.offset.y
            )

            return inRange(attackComponent.attackRange,playerTarget)
        }?:return false
    }

    fun hasEnemyNearby(): Boolean{
        return if (aiComponent.nearbyEntities.isNotEmpty() && lifeComps.getOrNull(aiComponent.nearbyEntities.first()) != null &&
            !lifeComps[aiComponent.nearbyEntities.first()].isDead){
            playerTarget.set(physicComps[aiComponent.nearbyEntities.first()].body.position)
            true
        }else{
            false
        }
    }

    fun startAttack() {
        attackComponent.attackState = AttackState.ATTACK
    }

    fun faceToKing() {

        playerEntity?.let { player->
            imageComponent.image.flipX = (physicComps[player].body.position.x > physicComps[entity].body.position.x)
        }
    }

    fun startTexture(textureType: TextureType) {
        animationComponent.nextTexture(textureType)
    }

    fun isFalling() = (physicComponent.body.linearVelocity.y < -0.3f)
    fun isJumping() = (physicComponent.body.linearVelocity.y > 0.3f)
    fun isGetHit() = lifeComponent.getHit
    fun canMove() : Boolean{
        return moveComponent?.let {
            it.moveRange != 0f
        }?: false
    }
    fun isAlive() = lifeComponent.life > 0f
    fun isMeleeAttack() : Boolean {
        return attackComponent.attackRange <= 1f
    }

    fun isRangeAttack(): Boolean {
        return attackComponent.attackRange > 1f
    }

}

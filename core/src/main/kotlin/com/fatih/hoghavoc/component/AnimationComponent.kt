package com.fatih.hoghavoc.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.TimeUtils
import com.fatih.hoghavoc.utils.DEFAULT_FRAME_DURATION

enum class AnimationType{
    DEAD,DOOR_IN,DOOR_OUT,FALL,GROUND,HIT,IDLE,RUN, ATTACK,BOX_FALL,BOX_GROUND,PICKING,JUMP_READY,LOOKING_OUT,
    PREPARE,READY,USE, JUMP,RUNNING,THROW,THROWING,ON
}

enum class TextureType{
    FALL,JUMP,IDLE
}

class AnimationComponent(
    var stateTime : Float = 0f,
    var frameDuration : Float = DEFAULT_FRAME_DURATION,
    var playMode: PlayMode = PlayMode.LOOP,
    var isAnimation : Boolean = true,
    var deadImageScale : Float = 1f,
    var isScaled : Boolean = false,
) {
    lateinit var animType : AnimationType
    var textureType: TextureType? = null
    lateinit var entityModel: EntityModel
    lateinit var animation : Animation<TextureRegionDrawable>
    lateinit var texture : TextureRegionDrawable
    var nextAnimation : String = ""
    var nextTexture : String = ""

    fun isAnimationDone(animType : AnimationType,playMode: PlayMode = PlayMode.NORMAL) : Boolean {
        return ( animation.isAnimationFinished(stateTime))
    }

    fun isAnimationDone(animType : AnimationType) : Boolean {
        if (entityModel == EntityModel.PIG_FIRE ) {
            println(animType)
            println(stateTime)
            println(playMode)
        }
        return ( this.animType == animType && animation.isAnimationFinished(stateTime))
    }

    fun isAttackAnimationDone() : Boolean {
        return animation.isAnimationFinished(stateTime + 0.2f)
    }

    fun nextAnimation(animationType: AnimationType,playMode: PlayMode = this.playMode,frameDuration: Float = this.frameDuration){
        isAnimation = true
        this.playMode = playMode
        val animType = when(animationType){
            AnimationType.IDLE ->{
                if (entityModel == EntityModel.PIG_FIRE) {
                    this.playMode = PlayMode.NORMAL
                    AnimationType.PREPARE
                } else animationType
            }
            AnimationType.DEAD ->{
                when{
                    entityModel != EntityModel.PIG && entityModel != EntityModel.KING && entityModel != EntityModel.KING_PIG ->{
                        deadImageScale = 1.15f
                        entityModel = EntityModel.PIG
                    }
                }
                animationType
            }
            AnimationType.ATTACK ->{
                when(entityModel){
                    EntityModel.PIG_BOMB->{
                        AnimationType.THROW
                    }
                    EntityModel.PIG_BOX->{
                        AnimationType.THROWING
                    }
                    else-> animationType
                }
            }

            else -> animationType
        }
        if (entityModel == EntityModel.PIG_FIRE) println("${entityModel.name.lowercase()}_${animType.name.lowercase()}")
        nextAnimation = "${entityModel.name.lowercase()}_${animType.name.lowercase()}"
        this.animType = animType
        this.frameDuration = frameDuration
        this.stateTime = 0f
    }

    fun nextTexture(textureType :TextureType){
        isAnimation = false
        nextTexture = "${entityModel.name.lowercase()}_${textureType.name.lowercase()}"
        this.stateTime = 0f
        this.textureType = textureType
    }

}

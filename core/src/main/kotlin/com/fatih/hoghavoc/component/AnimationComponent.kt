package com.fatih.hoghavoc.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.TimeUtils
import com.fatih.hoghavoc.utils.DEFAULT_FRAME_DURATION

enum class AnimationType{
    DEAD,DOOR_IN,DOOR_OUT,FALL,GROUND,HIT,IDLE,RUN, ATTACK,BOX_FALL,BOX_GROUND,PICKING,JUMP_READY,LOOKING_OUT,
    PREPARE,READY,USE, JUMP,RUNNING,THROW,THROWING,ON,SHOOT,OPENING,CLOSING,EXPLODE
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
    var animation : Animation<TextureRegionDrawable>? = null
    lateinit var texture : TextureRegionDrawable
    var nextAnimation : String = ""
    var nextTexture : String = ""
    var isDoor = false


    fun isAnimationDone(animType : AnimationType,playMode: PlayMode = PlayMode.NORMAL) : Boolean {
        return ( animation?.isAnimationFinished(stateTime)?:false)
    }

    fun isAnimationDone(animType : AnimationType) : Boolean {

        return ( this.animType == animType && animation?.isAnimationFinished(stateTime) ?: false)
    }

    fun isAttackAnimationDone(plusTime : Float = 0.2f) : Boolean {
        return animation?.isAnimationFinished(stateTime + plusTime) ?: false
    }


    fun nextAnimation(animationType: AnimationType,playMode: PlayMode = this.playMode,frameDuration: Float = this.frameDuration){
        isAnimation = true
        this.playMode = playMode
        val animType = when(animationType){
            AnimationType.IDLE ->{
                if (entityModel == EntityModel.PIG_FIRE) {
                    this.playMode = PlayMode.NORMAL
                    AnimationType.PREPARE
                }else if(entityModel == EntityModel.PIG_BOX_INSIDE){
                    setOf(AnimationType.LOOKING_OUT,AnimationType.JUMP_READY).random()
                }
                else animationType
            }
            AnimationType.RUN ->{
                if (entityModel == EntityModel.PIG_BOX_INSIDE){
                    AnimationType.JUMP
                }else animationType
            }
            AnimationType.DEAD ->{
                when{
                    entityModel != EntityModel.PIG && entityModel != EntityModel.KING && entityModel != EntityModel.KING_PIG ->{
                        deadImageScale = if (entityModel == EntityModel.PIG_FIRE) 1.5f else 1.15f
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
                    EntityModel.PIG_BOX_INSIDE->{
                        AnimationType.JUMP
                    }
                    else-> animationType
                }
            }

            else -> animationType
        }
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

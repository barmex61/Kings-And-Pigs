package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.utils.EMPTY_LINE
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.collections.map

@AllOf([AnimationComponent::class])
class AnimationSystem(
    private val animComps : ComponentMapper<AnimationComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val textureAtlas: TextureAtlas
) : IteratingSystem(){

    private lateinit var animationComponent: AnimationComponent
    private lateinit var imageComponent: ImageComponent
    private lateinit var physicComponent: PhysicComponent
    private val animCache = hashMapOf<String,Animation<TextureRegionDrawable>>()
    private val textureCache = hashMapOf<String,TextureRegionDrawable>()

    private fun getAnimation(nextAnimation: String,frameDuration : Float) : Animation<TextureRegionDrawable> = animCache.getOrPut(nextAnimation){
        val frames = textureAtlas.findRegions(nextAnimation)
        Animation(frameDuration,frames.map { TextureRegionDrawable(it) })
    }

    private fun getTexture(texturePath : String) : TextureRegionDrawable = textureCache.getOrPut(texturePath){
        TextureRegionDrawable(textureAtlas.findRegion(texturePath))
    }

    override fun onTickEntity(entity: Entity) {
        animationComponent = animComps[entity]
        imageComponent = imageComps[entity]
        physicComponent = physicComps[entity]
        animationComponent.run {
            if (isAnimation){

                if (nextAnimation.isNotEmpty()){
                    if (entityModel == EntityModel.PIG_FIRE) println("system $nextAnimation")
                    animation = getAnimation(nextAnimation,frameDuration)
                    animation.playMode = playMode
                    nextAnimation = EMPTY_LINE
                }

                if (!isScaled && animType == AnimationType.DEAD){
                    imageComponent.image.run {
                        setSize(width*deadImageScale,height*deadImageScale)
                    }
                    isScaled = true
                }
                imageComponent.image.drawable = animation.getKeyFrame(stateTime)
                stateTime += deltaTime
            }else{
                if (nextTexture.isNotEmpty()){
                    texture = getTexture(nextTexture)
                    nextTexture = EMPTY_LINE
                }
                imageComponent.image.drawable = texture
                stateTime = 0f
            }
        }
    }
}

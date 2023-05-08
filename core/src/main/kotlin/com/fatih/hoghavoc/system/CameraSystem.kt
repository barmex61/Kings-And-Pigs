package com.fatih.hoghavoc.system

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.TimeUtils
import com.fatih.hoghavoc.component.ImageComponent
import com.fatih.hoghavoc.component.PlayerComponent
import com.fatih.hoghavoc.events.MapChangeEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.tiled.height
import ktx.tiled.width
import kotlin.math.max
import kotlin.math.min


@AllOf([PlayerComponent::class,ImageComponent::class])
class CameraSystem (
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val gameStage : Stage,
    private val touchpad: Touchpad,
    private val attackImage : Image,
    ): IteratingSystem() , EventListener{

    private lateinit var imageComponent: ImageComponent
    private lateinit var playerComponent: PlayerComponent
    private var maxWidth = 0f
    private var maxHeight = 0f

    override fun onTickEntity(entity: Entity) {
        imageComponent = imageComps[entity]
        playerComponent = playerComps[entity]
        gameStage.apply {
            act(deltaTime)
            val viewportWidth = camera.viewportWidth
            val viewportHeight = camera.viewportHeight
            val maxHeight = max(0f, imageComponent.image.y + imageComponent.image.height)
            val maxWidth = max(0f, imageComponent.image.x + imageComponent.image.width)
            val offsetX = if(imageComponent.image.flipX) 1.8f else 1f
            val posX = (imageComponent.image.x + offsetX).coerceIn(
                min(viewportWidth/2f, maxWidth+viewportWidth/2f),
                max(viewportWidth/2f, maxWidth+viewportWidth/2f)
            )
            val posY = (imageComponent.image.y + 1f).coerceIn(
                min(viewportHeight/2f,maxHeight),
                max(viewportHeight/2f,maxHeight)
            )
            camera.position.set(posX, posY, camera.position.z)
            touchpad.setPosition(camera.position.x - viewportWidth/2.5f, camera.position.y - viewportWidth/5f)
            attackImage.setPosition(camera.position.x + viewportWidth/4f, camera.position.y - viewportWidth/5f)
            camera.update()
        }
    }

    override fun handle(event: Event?): Boolean {
        return when(event){
            is MapChangeEvent ->{
                maxWidth = event.map.width.toFloat()
                maxHeight = event.map.height.toFloat()
                true
            }
            else -> false
        }
    }
}

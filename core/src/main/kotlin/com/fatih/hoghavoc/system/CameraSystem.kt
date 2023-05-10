package com.fatih.hoghavoc.system

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.component.ImageComponent
import com.fatih.hoghavoc.component.PlayerComponent
import com.fatih.hoghavoc.events.MapChangeEvent
import com.github.quillraven.fleks.*
import ktx.tiled.height
import ktx.tiled.width


@AllOf([PlayerComponent::class,ImageComponent::class])
class CameraSystem (
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val gameStage : Stage,
    private val isMobile : Boolean
    ): IteratingSystem(interval = Fixed(1/300f)) , EventListener{

    private lateinit var imageComponent: ImageComponent
    private lateinit var playerComponent: PlayerComponent
    private var maxWidth = 0f
    private var maxHeight = 0f

    override fun onTickEntity(entity: Entity) {

    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {

        imageComponent = imageComps[entity]
        playerComponent = playerComps[entity]
        gameStage.apply {
            act(deltaTime)
            val viewportWidth = camera.viewportWidth
            val viewportHeight = camera.viewportHeight
            val offsetX = if(imageComponent.image.flipX) 1.8f else 1f
            val posX = (imageComponent.image.x + offsetX).coerceIn(
                viewportWidth/2f, viewportWidth + if (isMobile) 0f else viewportWidth/2f
            )
            val posY = (imageComponent.image.y + 1f).coerceIn(
                viewportHeight/2f,viewportHeight - 3f
            )
            camera.position.set(posX, posY, camera.position.z)
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

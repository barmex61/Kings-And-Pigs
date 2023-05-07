package com.fatih.hoghavoc.system

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
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
    private val gameStage : Stage
    ): IteratingSystem() , EventListener{

    private lateinit var imageComponent: ImageComponent
    private lateinit var playerComponent: PlayerComponent
    private var maxWidth = 0f
    private var maxHeight = 0f
    private var offset = 0f

    override fun onTickEntity(entity: Entity) {
        imageComponent = imageComps[entity]
        playerComponent = playerComps[entity]
        gameStage.apply {

            act(deltaTime)
            offset = if(imageComponent.image.flipX) 1.8f else 1f
            camera.position.set(
                (imageComponent.image.x + offset).coerceIn(
                    min(camera.viewportWidth/2f, maxWidth+camera.viewportWidth/2f),
                    max(camera.viewportWidth/2f, maxWidth+camera.viewportWidth/2f)
                ),
                (imageComponent.image.y + 1f).coerceIn(
                    min(camera.viewportHeight/2f,maxHeight),
                    max(camera.viewportHeight/2f,maxHeight)
                ),
                camera.position.z
            )
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

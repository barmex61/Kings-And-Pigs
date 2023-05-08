package com.fatih.hoghavoc.system

import com.badlogic.gdx.math.MathUtils
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


@AllOf([PlayerComponent::class,ImageComponent::class])
class CameraSystem (
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val gameStage : Stage,
    private val isMobile : Boolean
    ): IteratingSystem() , EventListener{

    private lateinit var imageComponent: ImageComponent
    private lateinit var playerComponent: PlayerComponent
    private var maxWidth = 0f
    private var maxHeight = 0f
    private var flipx = 0f

    override fun onTickEntity(entity: Entity) {
        imageComponent = imageComps[entity]
        playerComponent = playerComps[entity]
        gameStage.apply {
            flipx = if (imageComponent.image.flipX) 1.8f else 1f

            camera.position.set(
                (imageComponent.image.x + flipx).coerceIn(camera.viewportWidth/2f,camera.viewportWidth + if (isMobile) 0f else camera.viewportWidth/2f),
                imageComponent.image.y .coerceIn(camera.viewportHeight/2f,camera.viewportHeight) ,
                camera.position.z
            )
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

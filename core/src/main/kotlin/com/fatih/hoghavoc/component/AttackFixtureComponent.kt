package com.fatih.hoghavoc.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.fatih.hoghavoc.actors.FlipImage
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import ktx.math.vec2

class AttackFixtureComponent {
    var attackImage : FlipImage? = null
    var attackBody : Body? = null
    var delay : Float = 0f
    var imageSize : Vector2 = vec2()
    var maxDelay : Float = 0f
    lateinit var attackType : AttackType
    var animation : Animation<TextureRegionDrawable>? =null
    var animationStr : String = ""
    var stateTimer = 0f
    var destroyBodies : Boolean = false
    var boxPiecesHashMap : HashMap<FlipImage,Body?>? = null
    var boxPiecesBodyCreated : Boolean = false
    var explodeBody : Body? = null
    var hitPlayer : Boolean = false

    companion object{
        class AttackFixtureComponentListener(
            private val gameStage: Stage
        ) : ComponentListener<AttackFixtureComponent>{
            override fun onComponentAdded(entity: Entity, component: AttackFixtureComponent) {
                gameStage.addActor(component.attackImage)
                component.animationStr = when(component.attackType){
                    AttackType.BOMB->{
                        "bomb_on"
                    }
                    AttackType.BOX->{
                        "box_pieces"
                    }
                    else -> ""
                }
            }

            override fun onComponentRemoved(entity: Entity, component: AttackFixtureComponent) {
                gameStage.root.removeActor(component.attackImage)
                component.animationStr = ""

            }
        }
    }
}

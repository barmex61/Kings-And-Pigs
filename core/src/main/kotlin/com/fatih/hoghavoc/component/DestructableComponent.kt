package com.fatih.hoghavoc.component

import com.badlogic.gdx.physics.box2d.Body
import com.fatih.hoghavoc.actors.FlipImage


class DestructableComponent(
    var create: Boolean = false,
    var boxPiecesDestructTimer : Float = 5f,
    var boxPiecesBody : HashMap<FlipImage,Body>? = null
) {
    lateinit var itemModels : List<EntityModel>
    var body : Body? = null

}

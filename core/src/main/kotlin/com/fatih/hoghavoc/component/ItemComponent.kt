package com.fatih.hoghavoc.component

import com.fatih.hoghavoc.utils.DEFAULT_CONTACT_DELAY
import com.github.quillraven.fleks.Entity


class ItemComponent(
    var itemModel : EntityModel = EntityModel.DIAMOND,
    var extraLife : Float = 0f,
    var contactTimer : Float = DEFAULT_CONTACT_DELAY
){
    var collideEntity : Entity? = null
}

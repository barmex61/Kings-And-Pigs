package com.fatih.hoghavoc.component

import com.github.quillraven.fleks.Entity

class PlayerComponent(
    val nearbyEntities : MutableSet<Entity> = mutableSetOf()
)


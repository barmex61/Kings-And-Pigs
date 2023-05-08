package com.fatih.hoghavoc.events

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.Event
import com.github.quillraven.fleks.Entity

class MapChangeEvent(val map : TiledMap) : Event()
class PlayerHitEnemyEvent(val playerEntity : Entity , val enemyEntity: Entity) : Event()
class EnemyHitPlayerEvent(val playerEntity: Entity,val enemyEntity: Entity?) : Event()
class AttackEvent(val onAir:Boolean) : Event()
class EnemyDamageEvent(val entity:Entity) : Event()
class PlayerDamageEvent(val entity: Entity) : Event()
class PauseEvent(val pause : Boolean) : Event()
class DestroyBoxBodies : Event()
class RespawnPlayer : Event()

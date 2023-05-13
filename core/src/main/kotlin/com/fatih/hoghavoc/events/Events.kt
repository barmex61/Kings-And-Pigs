package com.fatih.hoghavoc.events

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.Event
import com.fatih.hoghavoc.component.AttackType
import com.github.quillraven.fleks.Entity

class MapChangeEvent(val map : TiledMap) : Event()
class CurrentMapChangeEvent(val mapStr : String) : Event()
class PlayerHitEnemyEvent(val playerEntity : Entity , val enemyEntity: Entity) : Event()
class EnemyHitPlayerEvent(val playerEntity: Entity,val enemyEntity: Entity,val soundPath : String?) : Event()
class AttackEvent(val soundPath : String,val isPlayerGetHit : Boolean = false) : Event()
class EnemyDamageEvent(val entity:Entity) : Event()
class EnemyAttackEvent(val entity:Entity) : Event()
class PlayerDamageEvent(val entity: Entity) : Event()
class PauseEvent(val pause : Boolean) : Event()
class RespawnPlayer : Event()
class DestroyWorld : Event()
class StartStageActionEvent : Event()
class JumpEvent() : Event()

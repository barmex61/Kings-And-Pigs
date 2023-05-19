package com.fatih.hoghavoc.events

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.Event
import com.fatih.hoghavoc.component.AttackType
import com.fatih.hoghavoc.component.LifeComponent
import com.github.quillraven.fleks.Entity

class MapChangeEvent(val map : TiledMap,val entity: Entity? = null) : Event()
class CurrentMapChangeEvent(val mapStr : String,val restart:Boolean = false) : Event()
class PlayerHitEnemyEvent(val playerEntity : Entity , val enemyEntity: Entity) : Event()
class EnemyHitPlayerEvent(val playerEntity: Entity,val enemyEntity: Entity,val soundPath : String?) : Event()
class AttackEvent(val soundPath : String,val isPlayerGetHit : Boolean = false) : Event()
class EnemyDamageEvent(val lifePercentage:Float,val attackType: AttackType,val isKing : Boolean) : Event()
class PlayerDamageEvent(val lifePercentage: Float) : Event()
class PauseEvent(val pause : Boolean) : Event()
class RespawnPlayer : Event()
class DestroyWorld : Event()
class StartStageActionEvent : Event()
object JumpEvent : Event()
class HealEvent(val heal:Float,val entity: Entity) : Event()
class ScoreEvent(val score: Int) : Event()
class ShowTextEvent(val text : String) : Event()
class ExtraLifeEvent(val extraLife : Int) : Event()
class FinalScoreEvent() : Event()

interface ScreenListener{
    fun setScreen(screenType: ScreenType)
}

enum class ScreenType{
    GAME,MENU
}

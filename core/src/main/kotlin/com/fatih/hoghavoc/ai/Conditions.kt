package com.fatih.hoghavoc.ai

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task

abstract class Condition : LeafTask<EnemyAiEntity>(){
    val entity : EnemyAiEntity
        get() = `object`

    abstract fun condition() : Boolean

    override fun execute(): Status {
        return if (condition()){
            Status.SUCCEEDED
        }else{
            Status.FAILED
        }
    }

    override fun copyTo(task: Task<EnemyAiEntity>) = task
}

class CanAttack : Condition(){
    override fun condition() = entity.canAttack()
}
class CanNotAttack : Condition(){
    override fun condition() = !entity.canAttack()
}

class IsEnemyNearby : Condition(){
    override fun condition() = entity.hasEnemyNearby()
}

class IsFalling : Condition(){
    override fun condition() = entity.isFalling()
}
class IsJumping: Condition(){
    override fun condition() = entity.isJumping()
}

class IsGetHit : Condition(){
    override fun condition() = entity.isGetHit()
}

class CanMove : Condition(){
    override fun condition() = entity.canMove()
}
class CanNotMove : Condition(){
    override fun condition() = !entity.canMove()
}
class IsMeleeAttack : Condition(){
    override fun condition() = entity.isMeleeAttack()
}
class IsRangeAttack : Condition(){
    override fun condition() = entity.isRangeAttack()
}
class IsFirePig : Condition(){
    override fun condition() = entity.isFirePig()
}
class IsReadyToFire : Condition(){
    override fun condition() = entity.doAttack
}

class CanNotFire : Condition(){
    override fun condition() = !entity.doAttack
}

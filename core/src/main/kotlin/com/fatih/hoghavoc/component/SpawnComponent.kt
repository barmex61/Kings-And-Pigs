package com.fatih.hoghavoc.component

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import ktx.math.vec2


enum class EntityModel(var identify : String = "") {
    KING,KING_PIG,PIG,BOMB,PIG_BOX,PIG_FIRE,PIG_BOMB,CANNON,BOX,DOOR,DIAMOND,HEARTH,PIG_BOX_INSIDE;
}

data class SpawnConfig(
    val model : EntityModel = EntityModel.KING,
    val bodyType: BodyType = BodyType.StaticBody,
    val canAttack : Boolean = false,
    val physicScaling : Vector2 = vec2(1f,1f),
    val imageScaling : Vector2 = vec2(1f,1f),
    val physicOffset : Vector2 = vec2(),
    val attackDelay : Float = 0.5f,
    val attackScaling : Float = 1f,
    val extraAttackRange : Float = 0f,
    val lifeScaling : Float = 0f,
    val maxLife : Float = 50f,
    val aiTreePath : String = "",
    val regeneration : Float = 2f,
    val lootable : Boolean = false,
    val speedScaling : Float = 1f,
    val categoryBit : Short = -1,
    val maskBits : Short = -2,
    val moveRange : Float = 0f,
    val attackType : AttackType = AttackType.MELEE_ATTACK,
    val attackFixtureDestroyDelay : Float = 0f,
    val collisionRange : Float = 0f,
    val doorOut : Boolean = false,
    val doorIn : Boolean = false,
    val attackDamage : IntRange = 0..1,
    val critChance : Float = 0f,
    val density : Float = 50f,
    val score : Int = 0
)

data class SpawnComponent (
    val location : Vector2 = vec2(),
    var name : String = "",
    var moveRange : Float = 0f,
    var identify : String = "",
    var width : Float = 0f,
    var height : Float = 0f,
    var flipX : Boolean = false
    )

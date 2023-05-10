package com.fatih.hoghavoc.utils

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.component.AnimationType
import com.fatih.hoghavoc.component.EntityModel
import kotlin.experimental.or

fun Stage.fireEvent(event: Event) = this.root.fire(event)

const val BOX = "Box"
const val KING = "King"
const val KING_PIG = "King_pig"
const val PIG = "Pig"
const val BOMB = "Bomb"
const val EMPTY_LINE = ""
const val PIG_BOX = "Pig_box"
const val PIG_FIRE = "Pig_fire"
const val PIG_BOMB = "Pig_bomb"
const val CANNON = "Cannon"

const val UNIT_SCALE = 1/16f
const val DEFAULT_ENTITY_SIZE = 2f
const val DEFAULT_FRAME_DURATION = 1/10f
const val DEFAULT_SPEED = 2f
const val TIME_BETWEEN_ATTACKS = 250L
const val DEAD_DELAY = 0.5f
const val HIT_DELAY = 0.2f
const val BOX_ATTACK_DELAY = 1f
const val MELEE_ATTACK_DELAY = 0.55f
const val BOMB_ATTACK_DELAY = 2f

const val KING_BIT : Short = 16
const val KING_PIG_BIT : Short = 1
const val PIG_BIT : Short = 2
const val BOX_BIT :Short = 4
const val GROUND_BIT : Short = 8
const val FOOT_BIT : Short = 32
const val ROOF_BIT : Short = 64
const val AXE_BIT : Short = 128
const val NOTHING_BIT : Short = 256
const val COLLISION_DETECT_BIT : Short = 512
const val ENEMY_FOOT_BIT :Short = 1024
const val ENEMY_AXE_BIT : Short = 2048
const val BOMB_BIT : Short = 4096
const val CANNON_BIT : Short = 8192


val KING_FOOT_ENABLE_COLLISION : Short = KING_PIG_BIT or CANNON_BIT or PIG_BIT or ROOF_BIT or GROUND_BIT or BOX_BIT or COLLISION_DETECT_BIT or ENEMY_AXE_BIT or BOMB_BIT
val KING_FOOT_DISABLE_COLLISION : Short = KING_PIG_BIT or PIG_BIT or GROUND_BIT or BOX_BIT or COLLISION_DETECT_BIT or ENEMY_AXE_BIT or BOMB_BIT or CANNON_BIT
val PIG_FOOT_ENABLE_COLLISION : Short = KING_BIT  or BOX_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or KING_PIG_BIT or BOMB_BIT or CANNON_BIT or PIG_BIT or ROOF_BIT
val PIG_FOOT_DISABLE_COLLISION : Short = KING_BIT  or BOX_BIT or AXE_BIT or GROUND_BIT or FOOT_BIT or KING_PIG_BIT or BOMB_BIT or CANNON_BIT or PIG_BIT


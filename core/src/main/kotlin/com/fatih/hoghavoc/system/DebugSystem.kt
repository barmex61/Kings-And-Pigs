package com.fatih.hoghavoc.system

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.IntervalSystem
import ktx.assets.disposeSafely
import ktx.graphics.use

class DebugSystem(
    private val physicWorld: World,
    private val gameStage : Stage,
) : IntervalSystem(enabled = true){

    private var box2DDebugRenderer: Box2DDebugRenderer? = null

    init {
        if (enabled){
            box2DDebugRenderer = Box2DDebugRenderer()
        }
    }

    override fun onTick() {
        box2DDebugRenderer?.render(physicWorld,gameStage.camera.combined)

    }

    override fun onDispose() {
        box2DDebugRenderer.disposeSafely()
    }
}

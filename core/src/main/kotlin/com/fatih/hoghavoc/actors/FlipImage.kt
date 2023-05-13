package com.fatih.hoghavoc.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Image

class FlipImage (textureRegion: TextureRegion? = null, var flipX : Boolean = false): Image(textureRegion) {


    override fun draw(batch: Batch?, parentAlpha: Float) {

        if (flipX){
            drawable.draw(batch,x+width,y,-width,height)
        }else{
            super.draw(batch, parentAlpha)
        }
    }
}

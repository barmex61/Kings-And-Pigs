package com.fatih.hoghavoc.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.assets.disposeSafely
import ktx.scene2d.Scene2DSkin
import ktx.style.label
import ktx.style.set
import ktx.style.skin

enum class Drawables{
    LIFE_BAR,KING_PROFILE,BIG_HEARTH,BIG_DIA,SMALL_HEARTH,KING_PORTRE,PIG_PORTRE,
    KING_PIG_PORTRE,HUD_BG,HP_BAR,MP_BAR,TEXT_BUBBLE,TEXT_LABEL,HP_BAR_SMALL,MP_BAR_SMALL,STATUS_BAR,TEXT_RECTANGLE
}

enum class Labels{
    FRAME,NOTHING,LARGE;
    val skinKey = this.name.lowercase()
}

enum class Fonts(
    val atlasRegion : String,
    val scaling : Float
){
    DEFAULT("ui_font",0.25f),
    BIG("ui_font",1f);
    val skinKey : String = "Font_${this.name.lowercase()}"
    val fontPath : String = "ui/$atlasRegion.fnt"
}

operator fun Skin.get(drawable:Drawables): Drawable = this.getDrawable(drawable.name.lowercase())
operator fun Skin.get(font:Fonts): BitmapFont = this.getFont(font.skinKey)

fun loadSkin(){
    Scene2DSkin.defaultSkin = skin(TextureAtlas("ui/uiObjects.atlas")){skin->
        Fonts.values().forEach { font->
            skin[font.skinKey] = BitmapFont(Gdx.files.internal(font.fontPath),skin.getRegion(font.atlasRegion)).apply {
                data.setScale(font.scaling)
                data.markupEnabled = true
            }
        }
        label(Labels.NOTHING.skinKey){
            font = skin[Fonts.DEFAULT]
        }
        label(Labels.LARGE.skinKey){
            font = skin[Fonts.BIG]
        }
        label(Labels.FRAME.skinKey){
            font = skin[Fonts.DEFAULT]
            background = skin[Drawables.TEXT_LABEL].apply {
                leftWidth = 30f
                rightWidth = 30f
                topHeight = 35f
                bottomHeight = 40f
            }
        }
    }
}

fun disposeSkin(){
    Scene2DSkin.defaultSkin.disposeSafely()
}

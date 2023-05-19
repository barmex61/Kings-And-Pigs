package com.fatih.hoghavoc.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.assets.disposeSafely
import ktx.scene2d.Scene2DSkin
import ktx.style.*

enum class Drawables{
    LIFE_BAR,KING_PROFILE,BIG_HEARTH,BIG_DIA,SMALL_HEARTH,KING_PORTRE,PIG_PORTRE,TOUCHPAD_KNOB,TOUCHPAD_BG,ATTACK,
    KING_PIG_PORTRE,HUD_BG,HP_BAR,MP_BAR,TEXT_BUBBLE,TEXT_LABEL,HP_BAR_SMALL,MP_BAR_SMALL,STATUS_BAR,TEXT_RECTANGLE,R,P,BACK,
    PIG_FIRE_PORTRE,PIG_BOX_PORTRE,PIG_BOMB_PORTRE,ITEM1,ITEM2,ITEM3,ITEM4,PLAY_ICON,GHOST_ICON,SCORE_ICON,MENU_BG,CIRCLE_BG,DOWN,UP,RIGHT,LEFT,SPACE
}

enum class TextFields{
    HEAD;
    val skinKey:String = this.name.lowercase()
}
enum class TextButtons{
    FRAME,ITEMS,HTP,TEXT,SCORE;
    val skinKey = this.name.lowercase()
}

enum class Labels{
    SCORE,MENU,LARGE,HIGHSCORE;
    val skinKey = this.name.lowercase()
}
enum class ImageButtons{
    BACK;
    val skinKey: String = this.name.lowercase()
}

enum class Fonts(
    val atlasRegion : String,
    val scaling : Float
){
    DEFAULT("ui_font",0.25f),
    MENU("menu_fnt",0.45f),
    BIG("ui_font",1f),
    ITEM("menu_fnt",0.35f);
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
        textField{
            this.font = skin[Fonts.ITEM]
            this.fontColor = Color.DARK_GRAY
        }
        textField(TextFields.HEAD.skinKey){
            this.font = skin[Fonts.MENU]
            this.fontColor = Color.DARK_GRAY
        }
        imageButton {
            imageUp = skin[Drawables.ATTACK]
            imageDown = skin[Drawables.ATTACK]
        }
        imageButton(ImageButtons.BACK.skinKey){
            imageUp = skin[Drawables.BACK]
            this.imageOver = skin.newDrawable(skin[Drawables.BACK],0.2f,0.1f,0.8f,1f)
            imageDown = skin[Drawables.BACK]
        }
        touchpad{
            background = skin[Drawables.TOUCHPAD_BG].apply {
                minWidth = 60f
                minHeight = 60f
            }
            knob = skin[Drawables.TOUCHPAD_KNOB].apply {
                minWidth = 25f
                minHeight = 25f
            }
        }

        label(Labels.SCORE.skinKey){
            fontColor = Color.WHITE
            font = skin[Fonts.ITEM]
        }
        label(Labels.MENU.skinKey){
            fontColor = Color.WHITE
            font = skin[Fonts.MENU]
            background = skin[Drawables.ITEM3].apply {
                leftWidth = 36f
                bottomHeight = 22f
            }
        }
        label(Labels.HIGHSCORE.skinKey){
            fontColor = Color.WHITE
            font = skin[Fonts.ITEM]
            background = skin.newDrawable(skin[Drawables.ITEM4],1F,1F,1F,1F).apply {
                minWidth = 175f
                minHeight = 20f
            }
        }
        label(Labels.LARGE.skinKey){
            fontColor = Color.RED
            font = skin[Fonts.BIG]
        }
        textButton(TextButtons.HTP.skinKey){
            this.font = skin[Fonts.ITEM]
            val drawable = skin.newDrawable(skin[Drawables.ITEM4],1F,1F,1F,1F).apply {
                minWidth = 125f
                minHeight = 20f
            }
            this.over = skin.newDrawable(skin[Drawables.ITEM4],0.3f,0.2f,0.4f,1F).apply {
                minWidth = 125f
                minHeight = 20f
            }
            this.checkedDown = skin.newDrawable(skin[Drawables.ITEM4],0.2f,0.15f,0.8f,1F).apply {
                minWidth = 125f
                minHeight = 20f
            }
            this.up = drawable


        }
        textButton(TextButtons.ITEMS.skinKey){
            font = skin[Fonts.ITEM]
            val drawable = skin.newDrawable(skin[Drawables.ITEM4],1F,1F,1F,1F).apply {

                minWidth = 125f
                minHeight = 20f

            }
            this.over = skin.newDrawable(skin[Drawables.ITEM4],0.3f,0.2f,0.4f,1F).apply {
                minWidth = 125f
                minHeight = 20f
            }
            this.checkedDown = skin.newDrawable(skin[Drawables.ITEM4],0.2f,0.15f,0.8f,1F).apply {
                minWidth = 125f
                minHeight = 20f
            }
            this.up = drawable

        }

        label(TextButtons.TEXT.skinKey){
            font = skin[Fonts.MENU]
            background = skin[Drawables.ITEM2].apply {
                leftWidth = 30f
                rightWidth = 30f
                topHeight = 35f
                bottomHeight = 40f
                minWidth = 40f
                minHeight = 80f
            }
        }
        textButton(TextButtons.FRAME.skinKey){
            font = skin[Fonts.DEFAULT]
            val drawable = skin.newDrawable(skin[Drawables.TEXT_LABEL],1F,1F,1F,1F).apply {

                minWidth = 125f
                minHeight = 20f

            }
            this.over = skin.newDrawable(skin[Drawables.ITEM4],0.3f,0.2f,0.4f,1F).apply {
                minWidth = 125f
                minHeight = 20f
            }
            this.checkedDown = skin.newDrawable(skin[Drawables.ITEM4],0.2f,0.15f,0.8f,1F).apply {
                minWidth = 125f
                minHeight = 20f
            }
            this.up = drawable

        }
    }
}



fun disposeSkin(){
    Scene2DSkin.defaultSkin.disposeSafely()
}

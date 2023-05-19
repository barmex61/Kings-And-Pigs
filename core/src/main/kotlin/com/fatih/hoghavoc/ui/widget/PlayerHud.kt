package com.fatih.hoghavoc.ui.widget

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.fatih.hoghavoc.ui.Drawables
import com.fatih.hoghavoc.ui.get
import ktx.actors.alpha
import ktx.scene2d.*

class PlayerHud(
    private val skin: Skin
) : WidgetGroup(), KGroup {


    private val imageList = mutableListOf<Image>()
    private val hud : Image = Image(skin[Drawables.HUD_BG]).apply {
        setSize(110f,37f)

    }
    private val lifeBar : Image = Image(skin[Drawables.LIFE_BAR]).apply {
        setPosition(hud.x,hud.y -34f)
    }
    private val profile : Image = Image(skin[Drawables.KING_PORTRE]).apply {
        setPosition(hud.x + 3f,hud.y+8f)
        setSize(22f,25f)
    }
    private val hp_bar : Image = Image(skin[Drawables.HP_BAR]).apply {
        setPosition(hud.x+29.7f,hud.y+21.8f)
        setSize(width * 110f / 500f,height * 37f / 150f)
    }

    private val mp_bar : Image = Image(skin[Drawables.MP_BAR]).apply {
        setPosition(hud.x+29.7f,hud.y+17.8f)
        setSize(width * 110f / 500f,height * 37f / 160f)
    }

    init {
        (1..3).forEach {
            imageList.add(Image(skin[Drawables.SMALL_HEARTH]).apply {
                setPosition(lifeBar.x + it * 11f, lifeBar.y + 10f)
            })
        }
        addActor(profile)
        addActor(hud)
        addActor(hp_bar)
        addActor(mp_bar)
        addActor(lifeBar)
        imageList.forEach {
            addActor(it)
        }

    }

    fun setExtraLife(extraLife: Int,duration:Float = 1f){
        if (imageList.isEmpty() ) return
        if (imageList.last().hasActions()) imageList.last().clearActions()
        if (imageList.size > extraLife){
                imageList.last().addAction(
                    Actions.sequence(
                        fadeOut(duration, Interpolation.pow3OutInverse),
                        object : Action() {
                            override fun act(delta: Float): Boolean {
                                val image = imageList.removeLast()
                                removeActor(image)
                                return true
                            }
                        }
                    )
                )

            }else if (imageList.size < extraLife){
               imageList.add(Image(skin[Drawables.SMALL_HEARTH]).apply {
                    setPosition(lifeBar.x + (imageList.size + 1) * 11f , lifeBar.y + 10f)
                }.also {
                   addActor(it)
               })
                imageList.last().addAction( fadeIn(duration, Interpolation.pow3OutInverse),)
                return
            }

    }

    fun reduceHealthBar(percentage : Float){
        hp_bar.clearActions()
        hp_bar.addAction(Actions.scaleTo(
            percentage.coerceIn(0f,1f),1f,1f, Interpolation.smooth2
        ))
    }

    override fun getPrefWidth() = lifeBar.drawable.minWidth
    override fun getPrefHeight() = lifeBar.drawable.minHeight
}

@Scene2dDsl
fun <T> KWidget<T>.playerHud(
    skin: Skin = Scene2DSkin.defaultSkin,
    init : PlayerHud.(T) -> Unit = {}
) : PlayerHud = actor(PlayerHud(skin),init)

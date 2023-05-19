package com.fatih.hoghavoc.ui.widget

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.fatih.hoghavoc.ui.Drawables
import com.fatih.hoghavoc.ui.get
import ktx.actors.alpha

import ktx.scene2d.*

class EnemyHud(
    val skin : Skin
) : WidgetGroup(), KGroup {


    private val hud_bg : Image = Image(skin[Drawables.STATUS_BAR])
    private val small_hp_bar : Image = Image(skin[Drawables.HP_BAR_SMALL]).apply {
        setPosition(hud_bg.x+28.6f,hud_bg.y+22.7f)
        setSize(47.8f,3.25f)
    }
    private val small_mp_bar : Image = Image(skin[Drawables.MP_BAR_SMALL]).apply {
        setPosition(hud_bg.x+28.4f,hud_bg.y+17.4f)
        setSize(41.8f,2.65f)
    }
    private val enemyPortre : Image = Image(skin[Drawables.PIG_PORTRE]).apply {
        setPosition(hud_bg.x+5f,hud_bg.y+ 7f)
    }

    init {
        addActor(hud_bg)
        addActor(small_hp_bar)
        addActor(small_mp_bar)
        addActor(enemyPortre)
    }


    fun reduceHealthBar(percentage: Float) {
        small_hp_bar.clearActions()
        small_hp_bar.addAction(
            Actions.scaleTo(percentage.coerceIn(0f,1f),1f,1f)
        )
        if (this.alpha != 0f) this.clearActions()
        this.addAction(Actions.sequence(
            Actions.fadeIn(0.7f, Interpolation.fade),
            Actions.delay(2f, fadeOut(1f, Interpolation.fade))
        ))
    }
    fun reduceManaBar(percentage: Float) {
        small_mp_bar.clearActions()
        small_mp_bar.addAction(
            Actions.scaleTo(
            percentage.coerceIn(0f,1f),1f,1f, Interpolation.smooth2
        ))
    }

    fun setImage(drawable:Drawable) {
        enemyPortre.drawable = drawable
    }

    fun showEnemyInfo() {

    }
}

@Scene2dDsl
fun <T> KWidget<T>.enemyHud(
    skin: Skin = Scene2DSkin.defaultSkin,
    init : EnemyHud.(T) -> Unit = {}
) : EnemyHud = actor(EnemyHud(skin),init)

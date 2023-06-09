package com.fatih.hoghavoc.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.fatih.hoghavoc.ui.Labels
import com.fatih.hoghavoc.ui.TextButtons
import com.fatih.hoghavoc.ui.model.GameModel
import com.fatih.hoghavoc.ui.widget.*
import ktx.actors.alpha
import ktx.scene2d.*

class GameView(
    isPhone : Boolean = false,
    model : GameModel,
    skin : Skin
) : Table(skin) , KTable {

    private val playerHud : PlayerHud
    private val enemyHud : EnemyHud
    private val textTable : Table
    private val scoreLabel : Label
    var touchPad : Touchpad? = null
    var attackHud : AttackHud? = null
    val changeAlphaLambda : (String,Boolean) -> Unit = {actor,setAlpha->
        when (actor) {
            "touchpad" -> {
                touchPad?.alpha = if (setAlpha) 0.7f else 0.25f
            }
            "attack" -> {
                attackHud?.apply {
                    alpha =  if (setAlpha) 1f else 0.25f
                    setAlpha(alpha)
                }
            }
            else -> {
                touchPad?.alpha = 0.25f
                attackHud?.alpha = 0.25f
            }
        }
    }

    init {
        setFillParent(true)

        top().left()
        playerHud = playerHud{
            it.top().left().padTop(5f).padLeft(5f).width(80f).expandX()
        }
        enemyHud = enemyHud{
            it.padTop(25f).width(80f).expandX()
            this.alpha = 0f
        }
        scoreLabel = label("[WHITE]SCORE : 0",style = Labels.SCORE.skinKey){
            it.left().top().padLeft(10f).padTop(5f).expandX()
        }
        row()
        textTable = table {
            label("Thus is a Thust", style = TextButtons.TEXT.skinKey){ labelCell->
                setAlignment(Align.topLeft)
                wrap = true
                labelCell.expand().fill()
            }
            it.width(200f).height(100f).colspan(3).padTop(5f)
            this.alpha = 0f
        }
        if (isPhone){
            row()
            touchPad = touchpad(10f,skin = skin){
                this.setSize(60f,60f)
                this.alpha = 0.25f
                it.padLeft(50f).left().bottom().colspan(1)
            }
            attackHud = attackHud{
                it.right().padRight(50f).bottom().colspan(1)
            }
        }

        model.playerLife.observe {percentage->
            reducePlayerHealth(percentage)
        }
        model.text.observe {str->
            showActor(textTable,str)
        }
        model.enemyLife.observe {percentage->
            reduceEnemyHealth(percentage)
        }
        model.enemyImage.observe {enemyDrawable->
            setEnemyImage(enemyDrawable)
        }
        model.score.observe{score->
            setScore(score)
        }
        model.playerExtraLife.observe { extraLife->
            setExtraLife(extraLife)
        }

    }

    fun addInputListener(inputListener: InputListener){
        touchPad?.addListener(inputListener)
        attackHud?.addListener(inputListener)
    }

    private fun setScore(score:Int){
        scoreLabel.setText("SCORE : $score")
    }


    private fun setExtraLife(extraLife : Int) = playerHud.setExtraLife(extraLife)
    private fun reducePlayerHealth(percentage:Float) = playerHud.reduceHealthBar(percentage)
    private fun reduceEnemyHealth(percentage: Float) = enemyHud.reduceHealthBar(percentage)
    fun reduceEnemyMana(percentage: Float) = enemyHud.reduceManaBar(percentage)

    private fun setEnemyImage(drawable:Drawable){
        enemyHud.setImage(drawable)
    }

    private fun showActor(actor: Actor, text:String = ""){
        if (actor == textTable) (textTable.children.first() as Label).setText(text)
        if (actor.alpha == 0f){
            actor.clearActions()
            actor.addAction(sequence(
                fadeIn(1f, Interpolation.fade),
                delay(1f,fadeOut(0.5f, Interpolation.fade))
            ))
        }else{
            actor.resetFadeOutDelay()
        }
    }



    companion object{
        fun Actor.resetFadeOutDelay(){
            this.actions.filterIsInstance<SequenceAction>()
                .lastOrNull()?.let { sequenceAction ->
                    val delay = sequenceAction.actions.last() as DelayAction
                    delay.time = 0f
                    delay.duration = 0f
                }
        }
    }

}

@Scene2dDsl
fun <T> KWidget<T>.gameView(
    isPhone: Boolean,
    model: GameModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init : GameView.(T) -> Unit = {},
): GameView = actor(GameView(isPhone,model,skin),init)

package com.fatih.hoghavoc.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.fatih.hoghavoc.ui.Labels
import com.fatih.hoghavoc.ui.widget.EnemyHud
import com.fatih.hoghavoc.ui.widget.PlayerHud
import com.fatih.hoghavoc.ui.widget.enemyHud
import com.fatih.hoghavoc.ui.widget.playerHud
import com.fatih.hoghavoc.ui.model.GameModel
import ktx.actors.alpha
import ktx.scene2d.*

class GameView(
    model : GameModel,
    skin : Skin
) : Table(skin) , KTable {

    private val playerHud : PlayerHud
    val enemyHud : EnemyHud
    val textTable : Table

    init {
        setFillParent(true)
        top().left()
        playerHud = playerHud(){
            it.top().left().padTop(5f).padLeft(5f).width(110f).expandX()
        }
        enemyHud = enemyHud(){
            it.padTop(25f).width(150f).expandX()
            this.alpha = 0f
        }
        label("",style = Labels.NOTHING.skinKey){
            it.expandX()
        }
        row()

        textTable = table {
            label("Thus is a Thust", style = Labels.FRAME.skinKey){ labelCell->
                setAlignment(Align.topLeft)
                wrap = true
                labelCell.expand().fill()
            }
            it.width(200f).height(150f).colspan(3).padTop(5f)
            this.alpha = 0f
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
        model.enemyImage.observe {enemyImage->
            setEnemyImage(enemyImage)
        }

    }

    fun reducePlayerLife() = playerHud.reduceLife()
    private fun reducePlayerHealth(percentage:Float) = playerHud.reduceHealthBar(percentage)
    private fun reduceEnemyHealth(percentage: Float) = enemyHud.reduceHealthBar(percentage)
    fun reduceEnemyMana(percentage: Float) = enemyHud.reduceManaBar(percentage)

    private fun setEnemyImage(image: Image){
        enemyHud.setImage(image)
    }

    private fun showActor(actor: Actor, text:String = ""){
        if (actor == textTable) (textTable.children.first() as Label).setText(text)
        if (actor.alpha == 0f){
            actor.clearActions()
            actor.addAction(sequence(
                fadeIn(2f, Interpolation.fade),
                delay(2f,fadeOut(0.5f, Interpolation.fade))
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
    model: GameModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init : GameView.(T) -> Unit = {},
): GameView = actor(GameView(model,skin),init)

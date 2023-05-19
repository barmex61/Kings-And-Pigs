package com.fatih.hoghavoc.ui.model

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Json
import com.fatih.hoghavoc.HogHavoc.Companion.preferences
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.*
import com.fatih.hoghavoc.ui.Drawables
import com.fatih.hoghavoc.ui.get
import com.fatih.hoghavoc.utils.Observable
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.preferences.get
import ktx.scene2d.Scene2DSkin
import ktx.style.skin


class GameModel (
    val skin : Skin = Scene2DSkin.defaultSkin,
    world: World,
    private val lifeComps : ComponentMapper<LifeComponent> = world.mapper(),
    ) : EventListener {


    val playerLife  : Observable<Float> = Observable(1f)
    val enemyLife : Observable<Float> = Observable(1f)
    val text  : Observable<String> = Observable("")
    val playerExtraLife : Observable<Int> = Observable(3)
    val enemyImage : Observable<Drawable> = Observable(skin[Drawables.PIG_PORTRE])
    val score : Observable<Int> = Observable(0)
    private val json = Json()


    override fun handle(event: Event): Boolean {
        return  when(event){
            is PlayerDamageEvent ->{
                println("playerdamage ${event.lifePercentage}")
                playerLife.setValue(event.lifePercentage)
                true
            }
            is ExtraLifeEvent ->{
                playerExtraLife.setValue(playerExtraLife.getValue() + event.extraLife)
                true
            }
            is HealEvent ->{
                lifeComps[event.entity].run {
                    if (life <= maxLife ){
                        val currentLife  = (event.heal + life.coerceAtLeast(0f)).coerceAtMost(maxLife)
                        playerLife.setValue(currentLife / maxLife)
                        life = currentLife
                    }
                }

                true
            }
            is ScoreEvent ->{
                score.setValue(score.getValue() + event.score)
                true
            }
            is ShowTextEvent ->{
                text.setValue(event.text)
                true
            }
            is FinalScoreEvent ->{
                val savedScoreJson = preferences.getString("Scores","")
                if (savedScoreJson.isEmpty()){
                    preferences.putString("Scores",json.toJson(arrayListOf(0,0,0,0,0)))
                    preferences.flush()
                }
                val savedScoreList = json.fromJson(ArrayList<Int>():: class.java,savedScoreJson)
                if (score.getValue() > savedScoreList.min()){
                    savedScoreList.remove(savedScoreList.min())
                    savedScoreList.add(score.getValue())
                }
                preferences.remove("Scores")
                preferences.flush()
                preferences.putString("Scores",json.toJson(savedScoreList))
                preferences.flush()
                println(savedScoreList)
                true
            }

            is EnemyDamageEvent ->{
                when(event.attackType){
                    AttackType.BOMB->{
                        enemyImage.setValue(skin[Drawables.PIG_BOMB_PORTRE])
                    }
                    AttackType.MELEE_ATTACK->{
                        if (event.isKing){
                            enemyImage.setValue(skin[Drawables.KING_PIG_PORTRE])
                        }else{
                            enemyImage.setValue(skin[Drawables.PIG_PORTRE])
                        }
                    }
                    AttackType.CANNON->{
                        enemyImage.setValue(skin[Drawables.PIG_FIRE_PORTRE])
                    }
                    AttackType.BOX->{
                        enemyImage.setValue(skin[Drawables.PIG_BOX_PORTRE])
                    }
                }
                enemyLife.setValue(event.lifePercentage)
                true
            }
            else -> false
        }
    }

}



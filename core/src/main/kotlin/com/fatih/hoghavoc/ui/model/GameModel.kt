package com.fatih.hoghavoc.ui.model

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.EnemyAttackEvent
import com.fatih.hoghavoc.events.EnemyDamageEvent
import com.fatih.hoghavoc.events.PlayerDamageEvent
import com.fatih.hoghavoc.ui.Drawables
import com.fatih.hoghavoc.ui.get
import com.fatih.hoghavoc.utils.Observable
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.scene2d.Scene2DSkin


class GameModel (
    world : World,
    val skin : Skin = Scene2DSkin.defaultSkin,
    ) : EventListener {

    private val lifeComps : ComponentMapper<LifeComponent> = world.mapper()
    private val enemyComps : ComponentMapper<EnemyComponent> = world.mapper()

    private val enemyImageHashMap = hashMapOf<Drawables,Image>()
    val playerLife  : Observable<Float> = Observable(1f)
    val enemyLife : Observable<Float> = Observable(1f)
    val text  : Observable<String> = Observable("")
    val enemyImage : Observable<Image> = Observable(Image(skin[Drawables.PIG_PORTRE]))


    override fun handle(event: Event): Boolean {
        return  when(event){
            is EnemyAttackEvent->{
                val drawable = if (enemyComps[event.entity].entityModel == EntityModel.PIG) Drawables.PIG_PORTRE else Drawables.KING_PIG_PORTRE
                enemyImage.setValue(enemyImageHashMap.getOrPut(drawable){
                    Image(skin[drawable])
                })
                lifeComps[event.entity].run {
                    enemyLife.setValue(life/maxLife)
                }
                true
            }
            is PlayerDamageEvent ->{
                lifeComps[event.entity].run {
                    playerLife.setValue(life/maxLife)
                }
                true
            }
            else -> false
        }
    }

}



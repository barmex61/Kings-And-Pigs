package com.fatih.hoghavoc.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.fatih.hoghavoc.events.*
import ktx.tiled.propertyOrNull


object AudioManager : EventListener {

    private val soundCache = hashMapOf<String,Sound>()
    private var randomPath = "audio/giant1.wav"
    private var random = 1
    private var music : Music? = null
    private var pitch = 1f


    override fun handle(event: Event?): Boolean {
        return when(event){
            is MapChangeEvent ->{
                music?.stop()
                music = event.map.propertyOrNull<String>("music")?.let {path->
                    Gdx.audio.newMusic(Gdx.files.internal(path)).apply {
                        isLooping = true
                        volume = 0.13f
                        play()
                    }
                }
                true
            }

            is AttackEvent ->{
                val path = event.soundPath
                var volume = 0.10f
                pitch = when (path) {
                    CANNON_EXPLOSION -> 0.5f
                    BOMB_SOUND -> {
                        volume = 0.07f
                        1.5f
                    }
                    else -> 1f
                }
                if (event.isPlayerGetHit){
                    val playerHitPath = if ((1..2).random() == 1) PLAYER_HIT else PLAYER_HIT2
                    soundCache.getOrPut(playerHitPath){
                        Gdx.audio.newSound(Gdx.files.internal(playerHitPath))
                    }.apply {
                        this.stop()
                        this.play(0.10f,1.08f,0f)
                    }
                }
                if (path.isEmpty()) return true
                soundCache.getOrPut(path){
                    Gdx.audio.newSound(Gdx.files.internal(path))
                }.play(volume, pitch,0f)

                true
            }
            is EnemyDamageEvent ->{
                random = (1..5).random()
                randomPath = "audio/giant$random.wav"
                soundCache.getOrPut(randomPath){
                    Gdx.audio.newSound(Gdx.files.internal(randomPath))
                }.play(0.06f,1.15f,0f)
                true
            }
            is PauseEvent -> {
                if (event.pause) {
                    music?.pause()
                    soundCache.values.forEach {
                        it.pause()
                    }
                } else {
                    music?.play()
                    soundCache.values.forEach {
                        it.resume()
                    }
                }
                true
            }

            is JumpEvent ->{
                val path = if((1..2).random() == 1) JUMP1 else JUMP2
                soundCache.getOrPut(path){
                    Gdx.audio.newSound(Gdx.files.internal(path))
                }.play(0.10f,1.15f,0f)
                true
            }
            else -> false
        }
    }
}

package com.fatih.hoghavoc.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.fatih.hoghavoc.events.AttackEvent
import com.fatih.hoghavoc.events.EnemyDamageEvent
import com.fatih.hoghavoc.events.MapChangeEvent
import com.fatih.hoghavoc.events.PauseEvent
import com.github.quillraven.fleks.IntervalSystem
import ktx.tiled.propertyOrNull


class AudioSystem : IntervalSystem() , EventListener {

    private val soundRequest = hashMapOf<String,Sound>()
    private val soundCache = hashMapOf<String,Sound>()
    private val attackOnGroundSoundPath = "audio/groundattack.ogg"
    private val attackOnAirSoundPath = "audio/flyattack.ogg"
    private var randomPath = "audio/giant1.wav"
    private var random = 1
    private var music : Music? = null

    override fun onTick() {
        soundRequest.values.forEach { sound ->
            sound.play(0.3f)
        }
        soundRequest.clear()
    }

    override fun handle(event: Event?): Boolean {
        return when(event){
            is MapChangeEvent ->{
                music?.stop()
                music = event.map.propertyOrNull<String>("music")?.let {path->
                    Gdx.audio.newMusic(Gdx.files.internal("audio/$path")).apply {
                        isLooping = true
                        volume = 0.15f
                        play()
                    }
                }
                true
            }
            is AttackEvent ->{
                if (event.onAir){
                    if (attackOnAirSoundPath in soundRequest) return true
                    soundRequest[attackOnAirSoundPath] = soundCache.getOrPut(attackOnAirSoundPath){
                        Gdx.audio.newSound(Gdx.files.internal(attackOnAirSoundPath))
                    }
                }else{
                    if (attackOnGroundSoundPath in soundRequest ) return true
                    soundRequest[attackOnGroundSoundPath] = soundCache.getOrPut(attackOnGroundSoundPath){
                        Gdx.audio.newSound(Gdx.files.internal(attackOnGroundSoundPath))
                    }
                }
                true
            }
            is EnemyDamageEvent ->{
                random = (1..5).random()
                randomPath = "audio/giant$random.wav"
                if (randomPath in soundRequest) return true
                soundRequest[randomPath] = soundCache.getOrPut(randomPath){
                    Gdx.audio.newSound(Gdx.files.internal(randomPath))
                }
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
            else -> false
        }
    }
}

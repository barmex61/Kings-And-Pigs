package com.fatih.hoghavoc.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.TimeUtils

class PerformanceProfiler {

    // Değişkenler, gerekli sınıfları tutmak için kullanılır
    lateinit var spriteBatch: SpriteBatch

    // Oyun dünyasının işlem hızını ölçmek için kullanılır
    var fpsLogger: FPSLogger = FPSLogger()

    // Bellek kullanımını izlemek için kullanılır
    var memoryUsage: MemoryUsage = MemoryUsage()

    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L

    fun start() {
        startTime = System.nanoTime()
    }

    fun stop() {
        elapsedTime = System.nanoTime() - startTime
    }

    fun getElapsedTime(): Long {
        return elapsedTime
    }

    fun create() {
        // Bu metod, performans profillemesi için gerekli sınıfları oluşturur
        spriteBatch = SpriteBatch()

        // Oyunun başlatılması sırasında çağrılır
        Gdx.app.log("PerformanceProfiler", "Created")
    }

    fun render() {
        // Bu metod, performans profillemesi için çerçeve çizimini yönetir

        // Başlangıç zamanını kaydedin
        val startTime = TimeUtils.nanoTime()

        // Çizim kodu burada yer alır

        // Bitiş zamanını kaydedin
        val endTime = TimeUtils.nanoTime()

        // Geçen süreyi hesaplayın
        val deltaTime = (endTime - startTime) / 1000000.0

        // Çerçeve hızını göstermek için kullanın
        fpsLogger.log()

        // Bellek kullanımını göstermek için kullanın
        memoryUsage.log()

        // Çerçeve çizim zamanını loglayın
        Gdx.app.log("PerformanceProfiler", "Frame drawing time: $deltaTime ms")
    }

    fun dispose() {
        // Bu metod, performans profillemesi için kullanılan kaynakları temizler
        spriteBatch.dispose()

        // Oyunun kapatılması sırasında çağrılır
        Gdx.app.log("PerformanceProfiler", "Disposed")
    }
}

// FPS ölçmek için kullanılır
class FPSLogger {
    private var fps: Int = 0
    private var lastTime: Long = 0

    fun log() {
        if (TimeUtils.nanoTime() - lastTime >= 1000000000) /* 1,000,000,000ns == one second */ {
            Gdx.app.log("FPSLogger", "fps: $fps")
            fps = 0
            lastTime = TimeUtils.nanoTime()
        }
        fps++
    }
}

// Bellek kullanımını izlemek için kullanılır
class MemoryUsage {
    fun log() {
        val memoryUsed = (Gdx.app.javaHeap / 1024.0 / 1024.0)
        Gdx.app.log("MemoryUsage", "Memory used: $memoryUsed MB")
    }
}

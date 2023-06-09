@file:JvmName("Lwjgl3Launcher")

package com.fatih.hoghavoc.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.fatih.hoghavoc.HogHavoc

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(HogHavoc(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("HogHavoc")
        setForegroundFPS(0)
        useVsync(false)
        setIdleFPS(0)
        setWindowedMode(640, 480)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}

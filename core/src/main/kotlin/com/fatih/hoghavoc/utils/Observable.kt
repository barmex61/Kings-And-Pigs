package com.fatih.hoghavoc.utils

class Observable<T>(private var value :T) {
    private var previousValue = value
    private val observers = mutableListOf<(T) -> Unit >()

    fun setValue(newValue : T){
        previousValue = value
        value = newValue
        notifyObservers()
    }

    fun getValue() : T = value

    fun getPreviousValue() : T = previousValue


    fun observe(observer: (T) -> Unit){
        observers.add(observer)
    }
    fun removeObserver(observer: (T) -> Unit){
        observers.remove(observer)
    }
    private fun notifyObservers(){
        for (observer in observers){
            observer(value)
        }
    }
    fun clearObservers(){
        observers.clear()
    }
}

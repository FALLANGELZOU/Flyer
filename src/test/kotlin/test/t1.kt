package test

import flyer.FEventBus
import flyer.subscribe.annotation.FSubscribe
import flyer.subscribe.FEvent
import flyer.subscribe.FSubscribedMethod


open class JoinEvent(msg: String): FEvent {
    private var msg = ""
    init {
        this.msg = msg
    }

    fun getMessage() {
        println(msg)
    }

}

class JoinAfterPartyEvent(msg: String) : JoinEvent(msg) {

}

class Bean(name: String) {
    private var name = "undefined"
    init {
        this.name = name
    }

    @FSubscribe
    fun onJoinEvent(event: JoinEvent){
        event.getMessage()
    }

    @FSubscribe
    fun onJoinAfterPartyEvent(event: JoinAfterPartyEvent) {
        event.getMessage()
    }
}


fun main() {
    val person = Bean("Alex")
    FEventBus.post(JoinEvent("join!"))
    FEventBus.register(person)
    FEventBus.post(JoinAfterPartyEvent("welcome!"))
    FEventBus.unregister(person)
    FEventBus.post(JoinEvent("join!"))
}
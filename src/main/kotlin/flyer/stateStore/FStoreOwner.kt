package flyer.stateStore

import flyer.subscribe.annotation.FSubscribe
import flyer.stateStore.stateEvent.StateChangeEvent

interface FStoreOwner {
    @FSubscribe
    fun onState(stateChangeEvent: StateChangeEvent)
}
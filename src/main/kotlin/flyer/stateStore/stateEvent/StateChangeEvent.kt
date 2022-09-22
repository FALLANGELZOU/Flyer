package flyer.stateStore.stateEvent

import flyer.stateStore.FState
import flyer.subscribe.FEvent

class StateChangeEvent(val state: FState) : FEvent {
}
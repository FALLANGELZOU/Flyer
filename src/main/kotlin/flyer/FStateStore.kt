package flyer

import flyer.stateStore.FState
import flyer.stateStore.FStore
import flyer.stateStore.FStoreOwner
import flyer.stateStore.FxStore

class FStateStore<S: FState>(state: S) : FStore<S> {
    private val realStore = FxStore(state)

    override fun withState(block: S.() -> Unit) {
        realStore.withState(block)
    }

    override fun setState(stateReducer: S.() -> S) {
        realStore.setState(stateReducer)
    }

    override fun subscribe(subscriber: FStoreOwner) {
        realStore.subscribe(subscriber)
    }

}
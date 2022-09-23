package test

import flyer.subscribe.annotation.FSubscribe
import flyer.stateStore.FState
import flyer.stateStore.FStoreOwner
import flyer.stateStore.FxStore
import flyer.stateStore.stateEvent.StateChangeEvent



class VM : FStoreOwner{
    data class TState(
        val a: Int
    ) : FState
    val store = FxStore(TState(1))

    @FSubscribe
    override fun onState(stateChangeEvent: StateChangeEvent) {
        println("当前的state值：" + stateChangeEvent.state)
    }

    init {
        store.subscribe(this)
    }
}

fun main() {
    val vm = VM()
    Thread {
        for(i in 1.. 1000) {
            vm.store.setState { copy(a = a + 1) }
        }
    }.start()

    val t1 = System.currentTimeMillis()
    for (i in 1..1000) {
        vm.store.setState {
            copy(a = a + 1)
        }
    }
    println(System.currentTimeMillis() - t1)


}
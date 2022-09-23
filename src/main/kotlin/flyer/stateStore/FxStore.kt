package flyer.stateStore

import flyer.FEventBus
import flyer.stateStore.stateEvent.FlushQueueEvent
import flyer.stateStore.stateEvent.StateChangeEvent
import flyer.subscribe.annotation.FSubscribe
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.locks.StampedLock



/**
 * 存放状态的容器，保证线程安全
 */
class FxStore<S: FState>(initState: S) : FStore<S>{
    private val stateOwner = StateOwner<S>()
    private val jobs = Jobs<S>()
    private class Jobs<S> {
        private val getStateQueue = LinkedList<(state: S) -> Unit>()
        private var setStateQueue = LinkedList<S.() -> S>()

        @Synchronized
        fun enqueueGetStateBlock(block: (state: S) -> Unit) {
            getStateQueue.add(block)
        }

        @Synchronized
        fun enqueueSetStateBlock(block: S.() -> S) {
            setStateQueue.add(block)
        }

        @Synchronized
        fun dequeueGetStateBlock(): ((state: S) -> Unit)? {
            if (getStateQueue.isEmpty()) return null
            return getStateQueue.removeFirst()
        }

        @Synchronized
        fun dequeueAllSetStateBlocks(): List<(S.() -> S)>? {
            if (setStateQueue.isEmpty()) return null
            val queue = setStateQueue
            setStateQueue = LinkedList()
            return queue
        }
    }

    /**
     * 获取当前state，异步
     */
    override fun withState(block: S.() -> Unit) {
        jobs.enqueueGetStateBlock(block)
        FEventBus.post(FlushQueueEvent())
    }

    /**
     * 设置当前state，异步
     */
    override fun setState(block: S.() -> S) {
        jobs.enqueueSetStateBlock(block)
        FEventBus.post(FlushQueueEvent())
    }

    /**
     * 订阅当前state
     */
    override fun subscribe(subscriber: FStoreOwner) {
        FEventBus.register(subscriber)
    }

    /**
     * 接收刷新jobs的信号
     */
    @FSubscribe
    fun onFlushQueue(event: FlushQueueEvent) {
        FlushQueueThread.submit { flushQueue() }
    }




    init {
        stateOwner.init(initState)
        FEventBus.register(this)
    }

    /**
     * 刷新读写队列，在读之前尽可能现写，保证每次读到的是最新值,
     * 单一线程维护
     */
    private tailrec fun flushQueue() {
        stateOwner.setValue(jobs.dequeueAllSetStateBlocks())
        val block = jobs.dequeueGetStateBlock() ?: return
        block.invoke(stateOwner.getValue())
        flushQueue()
    }

    /**
     * state 状态维护类
     */
    internal class StateOwner<S: FState> {
        private lateinit var value: S
        fun init(state: S) {
            value = state
        }

        fun getValue(): S {
            return value
        }

        fun setValue(setBlocks: List<S.() -> S>?) {
            setBlocks?.forEach { reducer ->
                value = value.reducer()
                post(value)
            }
        }

        private fun post(newValue: S) {
            FEventBus.post(StateChangeEvent(newValue))
        }
    }



}

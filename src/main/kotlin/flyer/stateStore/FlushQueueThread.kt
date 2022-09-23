package flyer.stateStore

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 所有刷新缓冲线程池
 */
object FlushQueueThread {
    private val flushThread = Executors.newSingleThreadExecutor()

    /**
     * 提交刷新
     */
    fun submit(block: () -> Unit) {
        flushThread.submit { block.invoke() }
    }

    /**
     * 关闭刷新线程
     */
    fun onDestroy() {
        // TODO： 有风险，暂时不知道怎么解决
    }
}
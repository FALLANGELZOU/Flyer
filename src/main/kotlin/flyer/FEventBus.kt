package flyer

import flyer.subscribe.annotation.FSubscribe
import flyer.subscribe.strategy.MethodInvokeStrategy
import flyer.subscribe.strategy.ReflectInvokeStrategy
import flyer.subscribe.FEvent
import flyer.subscribe.FSubscription
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass


object FEventBus {

    /**
     * 一个event需要分发给哪些方法
     */
    private val subscriptionsByEventType = mutableMapOf<KClass<FEvent>, CopyOnWriteArrayList<FSubscription>>()

    /**
     * 一个类订阅了哪些event
     */
    private val typesBySubscriber = mutableMapOf<Any, MutableList<KClass<FEvent>>>()

    /**
     * 代理策略
     */
    private var invokeStrategy: MethodInvokeStrategy = ReflectInvokeStrategy

    /**
     * 注册subscriber到EventBus，并获取其所有加了[FSubscribe] 注解的方法
     * @param subscriber 订阅者类
     */
    fun register(subscriber: Any) {
        val subscribedMethods = invokeStrategy.getAllSubscribedMethods(subscriber)
        for (method in subscribedMethods) {
            val eventClass = method.eventTypeClass

            // TODO: 改造成优先队列，实现priority排序
            subscriptionsByEventType[eventClass] ?: subscriptionsByEventType.let {
                it[eventClass] = CopyOnWriteArrayList<FSubscription>()
            }
            subscriptionsByEventType[eventClass]?.add(FSubscription(subscriber, method))

            typesBySubscriber[subscriber] ?: typesBySubscriber.let {
                it[subscriber] = mutableListOf()
            }
            typesBySubscriber[subscriber]?.add(eventClass)

        }
    }

    /**
     * 发送event
     * @param event 发送的事件
     */
    fun post(event: FEvent) {
        // 发给所有订阅该事件的订阅者
        val subscriptions = subscriptionsByEventType[event::class]
        subscriptions?.let {
            for (subscription in it) {
                invokeStrategy.invokeMethod(subscription, event)
            }
        }
    }

    /**
     * 取消订阅
     * @param subscriber
     */
    fun unregister(subscriber: Any) {
        val events = typesBySubscriber[subscriber]
        events?.forEach {
            subscriptionsByEventType[it]?.apply {
                for (subscription in this) {
                    if(subscription.subscriber == subscriber)
                        this.remove(subscription)
                }
            }
        }
        typesBySubscriber.remove(subscriber)
    }

    /**
     * 设置代理策略，默认使用反射
     * @param invokeStrategy 代理策略
     */
    fun setInvokeStrategy(invokeStrategy: MethodInvokeStrategy) {
        this.invokeStrategy = invokeStrategy
    }

}
package flyer.subscribe.strategy

import flyer.subscribe.FEvent
import flyer.subscribe.FSubscribedMethod
import flyer.subscribe.FSubscription
import java.util.concurrent.Flow.Subscription


interface MethodInvokeStrategy {
    fun getAllSubscribedMethods(subscriber: Any): MutableList<FSubscribedMethod>
    fun invokeMethod(subscription: FSubscription, event: FEvent)
}
package flyer.subscribe.strategy

import flyer.subscribe.annotation.FSubscribe
import flyer.subscribe.annotation.FThreadMode
import flyer.subscribe.FEvent
import flyer.subscribe.FSubscribedMethod
import flyer.subscribe.FSubscription
import javax.swing.text.html.FormSubmitEvent
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaMethod

object ReflectInvokeStrategy: MethodInvokeStrategy {
    /**
     * 获取所有订阅方法的信息
     */
    override fun getAllSubscribedMethods(subscriber: Any): MutableList<FSubscribedMethod> {
        val subscribedMethods = mutableListOf<FSubscribedMethod>()
        val clazz = subscriber::class
        for (function in clazz.declaredFunctions) {
            function.findAnnotation<FSubscribe>()?.let {
                val paramType = function.javaMethod?.parameterTypes
                if (paramType?.size == 1 && FEvent::class.java.isAssignableFrom(paramType[0])) {
                    subscribedMethods.add(
                        FSubscribedMethod(function, paramType[0].kotlin as KClass<FEvent>, it.threadMode, it.priority, function.name)
                    )
                }
            }
        }
        return subscribedMethods
    }

    /**
     * 代理加了[FSubscribe]注解的方法
     */
    override fun invokeMethod(subscription: FSubscription, event: FEvent) {
        val subscriber = subscription.subscriber
        val subscribedMethod = subscription.subscribedMethod
        val function = subscribedMethod.function
        when (subscribedMethod.threadMode) {
            FThreadMode.POSTING -> run {
                function.call(subscriber, event)
            }

            else -> {
                function.call(subscriber, event)
            }
        }
    }
}


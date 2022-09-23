package flyer.subscribe

import flyer.subscribe.annotation.FThreadMode
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * 订阅方法的详细参数
 * @param function: 方法反射
 * @param eventTypeClass:  订阅方法接收的event类型
 * @param threadMode:  线程模式
 * @param priority:    优先级
 * @param methodName:  方法名
 */
data class FSubscribedMethod(
    val function: KFunction<*>,
    val eventTypeClass: KClass<FEvent>,
    val threadMode: FThreadMode,
    val priority: Int,
    val methodName: String,
)
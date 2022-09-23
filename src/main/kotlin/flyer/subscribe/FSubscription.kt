package flyer.subscribe

/**
 * 订阅类，包含订阅者，订阅者的方法类详情
 */
data class FSubscription(
    //订阅者
    val subscriber: Any,
    //订阅者方法
    val subscribedMethod: FSubscribedMethod
)
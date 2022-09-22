package flyer.subscribe.annotation

import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class FSubscribe(
    val threadMode: FThreadMode = FThreadMode.POSTING,
    val priority: Int = 0
)



package flyer.stateStore


interface FStore<S: FState> {
    fun withState(block: S.() -> Unit)
    fun setState(stateReducer: S.() -> S)
    fun subscribe(subscriber: FStoreOwner)
}
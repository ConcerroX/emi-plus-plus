package concerrox.emixx.oreui.view

fun <T> liveData(initializer: () -> T): LiveData<T> {
    return LiveData(initializer())
}

class LiveData<T>(initialValue: T) {

    private val observers = mutableListOf<Observer<T>>()
    var value: T = initialValue
        set(value) {
            field = value
            observers.forEach { it.onChanged(value) }
        }

    fun observe(observer: Observer<T>) {
        if (!observers.contains(observer)) observers.add(observer)
    }

    fun interface Observer<T> {
        fun onChanged(value: T)
    }

}
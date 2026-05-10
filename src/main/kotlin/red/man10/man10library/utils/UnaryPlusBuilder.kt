package red.man10.man10library.utils

class UnaryPlusBuilder<T: Any> {
    private val list = mutableListOf<T>()

    operator fun T.unaryPlus() {
        list.add(this)
    }

    fun build(): List<T> {
        return list
    }
}
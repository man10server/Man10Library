package red.man10.man10library.inventory.itemStack.input

interface IInputParser<T> {

    fun parse(input: String): T?
}
package red.man10.man10library.inventory.builtin

import net.kyori.adventure.text.Component
import red.man10.man10library.inventory.MInventory
import java.util.concurrent.ConcurrentHashMap

//TODO: KDocの追加
@Suppress("unused")
abstract class MultiMInventory(
    title: Component,
    row: Int
): MInventory(title, row) {

    constructor(title: String, row: Int): this(Component.text(title), row)

    var currentSection: String? = null
        private set

    protected open val clearInventoryWhenSectionChanged: Boolean = true

    protected val sections = ConcurrentHashMap<String, MInventory.() -> Unit>()

    protected fun section(name: String, init: MInventory.() -> Unit) {
        sections[name] = init
        if (currentSection == null) {
            currentSection = name
        }
    }

    fun switchSection(sectionName: String) {
        currentSection = sectionName
        render()
    }

    abstract fun createSections()

    override fun renderContents() {
        sections.clear()
        createSections()

        val currentSection = currentSection ?: return
        val section = sections[currentSection] ?: return

        if (clearInventoryWhenSectionChanged) {
            clear()
        }

        section()
    }
}
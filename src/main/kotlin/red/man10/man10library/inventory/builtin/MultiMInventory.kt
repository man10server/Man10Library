package red.man10.man10library.inventory.builtin

import net.kyori.adventure.text.Component
import red.man10.man10library.inventory.MInventory
import java.util.concurrent.ConcurrentHashMap

//TODO: KDocの追加
@Suppress("unused")
abstract class MultiMInventory(
    title: Component,
    val row: Int
): MInventory(title, row) {

    var currentSection: String? = null
        private set

    protected var clearInventoryWhenSectionChanged: Boolean = true

    protected val sections = ConcurrentHashMap<String, MInventory>()

    protected abstract val sectionDefinitions:
            MultiMInventory.() -> Unit

    private var sectionDefinitionsInitialized: Boolean = false

    protected fun section(name: String, inventory: MInventory) {
        sections[name] = inventory
    }

    protected fun section(
        name: String,
        init: MultiMInventorySection.() -> Unit
    ) {
        val section = MultiMInventorySection(init)
        sections[name] = section
    }

    fun switchSection(sectionName: String) {
        currentSection = sectionName
        renderContents()
    }

    override fun renderContents() {
        if (!sectionDefinitionsInitialized) {
            sectionDefinitions()
            sectionDefinitionsInitialized = true
        }
        val currentSection = currentSection ?: return
        val section = sections[currentSection] ?: return

        section.renderContents()

        if (clearInventoryWhenSectionChanged) {
            clear()
        }

        section.items.forEach { (index, item) ->
            set(index, item)
        }
    }

    inner class MultiMInventorySection(
        val init: MultiMInventorySection.() -> Unit
    ): MInventory(Component.empty(), row) {

        override val renderOnSet: Boolean = false

        init {
            init()
        }

        override fun renderContents() {
            // セクションの内容はMultiMInventoryのrenderContentsで描画されるため、ここでは何もしない
        }
    }
}
package red.man10.man10library.event

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import red.man10.man10library.MJavaPlugin

class MEventUnit<T: Event>(
    private val clazz: Class<T>,
    priority: EventPriority = EventPriority.NORMAL,
    private val handler: (T) -> Unit
): Listener, EventExecutor {

    init {
        Bukkit.getPluginManager().registerEvent(clazz, this, priority, this, MJavaPlugin.plugin)
    }

    override fun execute(listener: Listener, event: Event) {
        if (!clazz.isInstance(event)) return
        handler(clazz.cast(event))
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
    }
}
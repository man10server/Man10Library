package red.man10.man10library.inventory.itemStack.input

import org.bukkit.Location
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import red.man10.man10library.event.MEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
object MInputItemManager {

    private val mEvent = MEvent()
    internal val inputSessions = ConcurrentHashMap<UUID, InputSession>()

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val parserMap = mutableMapOf<Class<*>, IInputParser<*>>(
        String::class.java to StringParser,
        java.lang.String::class.java to StringParser,
        Int::class.java to IntParser,
        Integer::class.java to IntParser,
        Double::class.java to DoubleParser,
        java.lang.Double::class.java to DoubleParser,
        Float::class.java to FloatParser,
        java.lang.Float::class.java to FloatParser,
        Long::class.java to LongParser,
        java.lang.Long::class.java to LongParser,
        Boolean::class.java to BooleanParser,
        java.lang.Boolean::class.java to BooleanParser,
        Location::class.java to LocationParser,
    )

    init {
        mEvent.register<PlayerCommandPreprocessEvent> { e ->
            val session = inputSessions[e.player.uniqueId] ?: return@register
            if (e.message.isBlank()) return@register

            e.isCancelled = true

            val input = e.message.trim()
            if (input.equals("/cancel", ignoreCase = true)) {
                session.onCancelled(e.player)
            } else {
                val success = session.onEnter(input, e.player)
                if (!success) return@register
            }

            inputSessions.remove(e.player.uniqueId)
        }
    }

    fun registerParser(type: Class<*>, parser: IInputParser<*>) {
        parserMap[type] = parser
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> parseInput(input: String, type: Class<T>): T? {
        // Enumだけ特別扱い。大文字小文字を無視してマッチさせる。
        if (type.isEnum) {
            val enumConstants = type.enumConstants ?: return null
            return enumConstants.firstOrNull { it.toString().equals(input, ignoreCase = true) }
        }

        val parser = parserMap[type] ?: return null
        return (parser as IInputParser<T>).parse(input)
    }
}
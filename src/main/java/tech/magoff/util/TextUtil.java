package tech.magoff.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;

public final class TextUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Map<Character, String> SMALL_CAPS_MAP = new HashMap<>();

    static {
        // Латиница
        SMALL_CAPS_MAP.put('a', "ᴀ");
        SMALL_CAPS_MAP.put('b', "ʙ");
        SMALL_CAPS_MAP.put('c', "ᴄ");
        SMALL_CAPS_MAP.put('d', "ᴅ");
        SMALL_CAPS_MAP.put('e', "ᴇ");
        SMALL_CAPS_MAP.put('f', "ꜰ");
        SMALL_CAPS_MAP.put('g', "ɢ");
        SMALL_CAPS_MAP.put('h', "ʜ");
        SMALL_CAPS_MAP.put('i', "ɪ");
        SMALL_CAPS_MAP.put('j', "ᴊ");
        SMALL_CAPS_MAP.put('k', "ᴋ");
        SMALL_CAPS_MAP.put('l', "ʟ");
        SMALL_CAPS_MAP.put('m', "ᴍ");
        SMALL_CAPS_MAP.put('n', "ɴ");
        SMALL_CAPS_MAP.put('o', "ᴏ");
        SMALL_CAPS_MAP.put('p', "ᴘ");
        SMALL_CAPS_MAP.put('q', "ǫ");
        SMALL_CAPS_MAP.put('r', "ʀ");
        SMALL_CAPS_MAP.put('s', "ꜱ");
        SMALL_CAPS_MAP.put('t', "ᴛ");
        SMALL_CAPS_MAP.put('u', "ᴜ");
        SMALL_CAPS_MAP.put('v', "ᴠ");
        SMALL_CAPS_MAP.put('w', "ᴡ");
        SMALL_CAPS_MAP.put('x', "x");
        SMALL_CAPS_MAP.put('y', "ʏ");
        SMALL_CAPS_MAP.put('z', "ᴢ");

        // Кириллица
        SMALL_CAPS_MAP.put('а', "ᴀ");
        SMALL_CAPS_MAP.put('б', "б");
        SMALL_CAPS_MAP.put('в', "в");
        SMALL_CAPS_MAP.put('г', "г");
        SMALL_CAPS_MAP.put('д', "д");
        SMALL_CAPS_MAP.put('е', "ᴇ");
        SMALL_CAPS_MAP.put('ё', "ᴇ̈");
        SMALL_CAPS_MAP.put('ж', "ж");
        SMALL_CAPS_MAP.put('з', "з");
        SMALL_CAPS_MAP.put('и', "и");
        SMALL_CAPS_MAP.put('й', "й");
        SMALL_CAPS_MAP.put('к', "ᴋ");
        SMALL_CAPS_MAP.put('л', "л");
        SMALL_CAPS_MAP.put('м', "ᴍ");
        SMALL_CAPS_MAP.put('н', "н");
        SMALL_CAPS_MAP.put('о', "ᴏ");
        SMALL_CAPS_MAP.put('п', "п");
        SMALL_CAPS_MAP.put('р', "ᴘ");
        SMALL_CAPS_MAP.put('с', "с");
        SMALL_CAPS_MAP.put('т', "ᴛ");
        SMALL_CAPS_MAP.put('у', "у");
        SMALL_CAPS_MAP.put('ф', "ф");
        SMALL_CAPS_MAP.put('х', "х");
        SMALL_CAPS_MAP.put('ц', "ц");
        SMALL_CAPS_MAP.put('ч', "ч");
        SMALL_CAPS_MAP.put('ш', "ш");
        SMALL_CAPS_MAP.put('щ', "щ");
        SMALL_CAPS_MAP.put('ъ', "ъ");
        SMALL_CAPS_MAP.put('ы', "ы");
        SMALL_CAPS_MAP.put('ь', "ь");
        SMALL_CAPS_MAP.put('э', "э");
        SMALL_CAPS_MAP.put('ю', "ю");
        SMALL_CAPS_MAP.put('я', "я");
    }

    private TextUtil() {}

    /**
     * Конвертирует обычный текст в стильный Small Caps (капитель).
     */
    public static String toSmallCaps(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            char lower = Character.toLowerCase(c);
            sb.append(SMALL_CAPS_MAP.getOrDefault(lower, String.valueOf(c)));
        }
        return sb.toString();
    }

    /**
     * Парсит MiniMessage строку в Adventure Component.
     */
    public static Component parse(String miniMessageString) {
        return MM.deserialize(miniMessageString);
    }

    /**
     * Префикс плагина в стиле 2026: ледяной градиент + стрелочка
     */
    public static String prefix() {
        return "<gradient:#00f2fe:#4facfe><b>❄ ᴡɪɴᴛᴇʀᴄʟᴀꜱʜ</b></gradient> <dark_gray>»</dark_gray> ";
    }
}

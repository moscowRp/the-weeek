package tech.magoff.loot;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import tech.magoff.theweeek;
import tech.magoff.util.ItemBuilder;
import tech.magoff.util.TextUtil;

public class FrostItems {

    public static NamespacedKey key(String id) {
        return new NamespacedKey(theweeek.getInstance(), id);
    }

    // ============ Осколок Вечной Зимы ============
    public static ItemStack frostShard() {
        return new ItemBuilder(Material.PRISMARINE_SHARD)
                .name(TextUtil.parse("<gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("Осколок Вечной Зимы") + "</b></gradient>"))
                .lore(
                        TextUtil.parse("<gray>" + TextUtil.toSmallCaps("Осколок древней ледяной силы.") + "</gray>"),
                        Component.empty(),
                        TextUtil.parse("<#4facfe>✦ <gray>" + TextUtil.toSmallCaps("Собери 3") + " <dark_gray>→</dark_gray> <gradient:#00f2fe:#4facfe>" + TextUtil.toSmallCaps("Сердце Зимы") + "</gradient>"),
                        TextUtil.parse("<#4facfe>✦ <gray>" + TextUtil.toSmallCaps("Собери 5") + " <dark_gray>→</dark_gray> <gradient:#b176ff:#e0c3fc>" + TextUtil.toSmallCaps("Корона Метели") + "</gradient>"),
                        Component.empty(),
                        TextUtil.parse("<gradient:#ffd200:#f7971e><b>✦ " + TextUtil.toSmallCaps("ЛЕГЕНДАРНЫЙ РЕСУРС") + "</b></gradient>")
                )
                .persistentData(key("frost_item"), "frost_shard")
                .glowing()
                .build();
    }

    // ============ Сердце Зимы ============
    public static ItemStack heartOfWinter() {
        return new ItemBuilder(Material.HEART_OF_THE_SEA)
                .name(TextUtil.parse("<gradient:#43e97b:#38f9d7><b>❄ " + TextUtil.toSmallCaps("Сердце Зимы") + "</b></gradient>"))
                .lore(
                        TextUtil.parse("<gray>" + TextUtil.toSmallCaps("Пульсирующее ядро вечного мороза.") + "</gray>"),
                        Component.empty(),
                        TextUtil.parse("<#38f9d7>▪ <gray>" + TextUtil.toSmallCaps("Frost Walker II при ходьбе") + "</gray>"),
                        TextUtil.parse("<#38f9d7>▪ <gray>" + TextUtil.toSmallCaps("Иммунитет к замедлению от метели") + "</gray>"),
                        Component.empty(),
                        TextUtil.parse("<dark_gray>[" + TextUtil.toSmallCaps("Держи в инвентаре для работы") + "]</dark_gray>"),
                        Component.empty(),
                        TextUtil.parse("<gradient:#f857a6:#ff5858><b>✦ " + TextUtil.toSmallCaps("МИФИЧЕСКИЙ АРТЕФАКТ") + "</b></gradient>")
                )
                .persistentData(key("frost_item"), "heart_of_winter")
                .glowing()
                .build();
    }

    // ============ Корона Метели ============
    public static ItemStack blizzardCrown() {
        return new ItemBuilder(Material.DIAMOND_HELMET)
                .name(TextUtil.parse("<gradient:#b176ff:#e0c3fc><b>❄ " + TextUtil.toSmallCaps("Корона Метели") + "</b></gradient>"))
                .lore(
                        TextUtil.parse("<gray>" + TextUtil.toSmallCaps("Корона владычицы вечных льдов.") + "</gray>"),
                        Component.empty(),
                        TextUtil.parse("<#b176ff>▪ <gray>" + TextUtil.toSmallCaps("Аура: Враги в радиусе 8 блоков") + "</gray>"),
                        TextUtil.parse("  <gray>" + TextUtil.toSmallCaps("получают постоянный эффект") + " <#00f2fe>" + TextUtil.toSmallCaps("Slowness II") + "</#00f2fe></gray>"),
                        Component.empty(),
                        TextUtil.parse("<gradient:#f857a6:#ff5858><b>✦ " + TextUtil.toSmallCaps("МИФИЧЕСКИЙ РЕЛИКТ") + "</b></gradient>")
                )
                .persistentData(key("frost_item"), "blizzard_crown")
                .enchant("protection", 5)
                .enchant("unbreaking", 4)
                .unbreakable()
                .glowing()
                .build();
    }

    // ============ Кулак Мороза (лут Голема) ============
    public static ItemStack fistOfFrost() {
        return new ItemBuilder(Material.MACE)
                .name(TextUtil.parse("<gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("Кулак Мороза") + "</b></gradient>"))
                .lore(
                        TextUtil.parse("<gray>" + TextUtil.toSmallCaps("Выкован из ледяных глыб Ледяного Голема.") + "</gray>"),
                        Component.empty(),
                        TextUtil.parse("<#00f2fe>▪ <gray>" + TextUtil.toSmallCaps("Накладывает") + " <#4facfe>" + TextUtil.toSmallCaps("Slowness III") + "</#4facfe> <gray>" + TextUtil.toSmallCaps("на 3 сек") + "</gray>"),
                        Component.empty(),
                        TextUtil.parse("<dark_gray>" + TextUtil.toSmallCaps("Дроп: Ледяной Голем") + "</dark_gray>"),
                        Component.empty(),
                        TextUtil.parse("<gradient:#ffd200:#f7971e><b>✦ " + TextUtil.toSmallCaps("ЛЕГЕНДАРНОЕ ОРУЖИЕ") + "</b></gradient>")
                )
                .persistentData(key("frost_item"), "fist_of_frost")
                .enchant("sharpness", 6)
                .enchant("unbreaking", 4)
                .glowing()
                .build();
    }

    // ============ Лук Вьюги (лут Королевы) ============
    public static ItemStack blizzardBow() {
        return new ItemBuilder(Material.BOW)
                .name(TextUtil.parse("<gradient:#667eea:#764ba2><b>❄ " + TextUtil.toSmallCaps("Лук Вьюги") + "</b></gradient>"))
                .lore(
                        TextUtil.parse("<gray>" + TextUtil.toSmallCaps("Стрелы пронзают воздух ледяным холодом.") + "</gray>"),
                        Component.empty(),
                        TextUtil.parse("<#667eea>▪ <gray>" + TextUtil.toSmallCaps("Стрелы накладывают") + " <#764ba2>" + TextUtil.toSmallCaps("Slowness II") + "</#764ba2> <gray>" + TextUtil.toSmallCaps("на 4 сек") + "</gray>"),
                        Component.empty(),
                        TextUtil.parse("<dark_gray>" + TextUtil.toSmallCaps("Дроп: Королева Метели") + "</dark_gray>"),
                        Component.empty(),
                        TextUtil.parse("<gradient:#ffd200:#f7971e><b>✦ " + TextUtil.toSmallCaps("ЛЕГЕНДАРНОЕ ОРУЖИЕ") + "</b></gradient>")
                )
                .persistentData(key("frost_item"), "blizzard_bow")
                .enchant("infinity", 1)
                .enchant("power", 6)
                .enchant("unbreaking", 4)
                .glowing()
                .build();
    }

    // ============ Мешок Санты (лут Санты) ============
    public static ItemStack santaBag() {
        return new ItemBuilder(Material.RED_SHULKER_BOX)
                .name(TextUtil.parse("<gradient:#ff0844:#ffb199><b>🎁 " + TextUtil.toSmallCaps("Мешок Санты") + "</b></gradient>"))
                .lore(
                        TextUtil.parse("<gray>" + TextUtil.toSmallCaps("Набит редкими новогодними сокровищами.") + "</gray>"),
                        Component.empty(),
                        TextUtil.parse("<dark_gray>" + TextUtil.toSmallCaps("Дроп: Тёмный Санта") + "</dark_gray>"),
                        Component.empty(),
                        TextUtil.parse("<gradient:#ffd200:#f7971e><b>✦ " + TextUtil.toSmallCaps("ЛЕГЕНДАРНЫЙ СУНДУК") + "</b></gradient>")
                )
                .persistentData(key("frost_item"), "santa_bag")
                .glowing()
                .build();
    }

    public static boolean isFrostItem(ItemStack item, String id) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(key("frost_item"), PersistentDataType.STRING)
                && id.equals(meta.getPersistentDataContainer().get(key("frost_item"), PersistentDataType.STRING));
    }

    public static boolean isAnyFrostItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(key("frost_item"), PersistentDataType.STRING);
    }
}

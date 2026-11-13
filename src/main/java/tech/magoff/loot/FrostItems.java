package tech.magoff.loot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import tech.magoff.theweeek;
import tech.magoff.util.ItemBuilder;

public class FrostItems {

    public static NamespacedKey key(String id) {
        return new NamespacedKey(theweeek.getInstance(), id);
    }

    // ============ Осколок Вечной Зимы ============
    public static ItemStack frostShard() {
        return new ItemBuilder(Material.PRISMARINE_SHARD)
                .name(Component.text("❄ Осколок Вечной Зимы", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                .lore(
                        Component.text("Осколок древней зимней силы", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Собери 3 → Сердце Зимы", NamedTextColor.DARK_AQUA),
                        Component.text("Собери 5 → Корона Метели", NamedTextColor.DARK_AQUA),
                        Component.empty(),
                        Component.text("✦ ЛЕГЕНДАРНЫЙ", NamedTextColor.GOLD)
                )
                .persistentData(key("frost_item"), "frost_shard")
                .glowing()
                .build();
    }

    // ============ Сердце Зимы ============
    public static ItemStack heartOfWinter() {
        return new ItemBuilder(Material.HEART_OF_THE_SEA)
                .name(Component.text("❄ Сердце Зимы", NamedTextColor.DARK_AQUA).decorate(TextDecoration.BOLD))
                .lore(
                        Component.text("Frost Walker II + иммунитет", NamedTextColor.GRAY),
                        Component.text("к замедлению от ивентов", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Держи в руке для эффекта", NamedTextColor.GREEN),
                        Component.empty(),
                        Component.text("✦ МИФИЧЕСКИЙ", NamedTextColor.LIGHT_PURPLE)
                )
                .persistentData(key("frost_item"), "heart_of_winter")
                .glowing()
                .build();
    }

    // ============ Корона Метели ============
    public static ItemStack blizzardCrown() {
        return new ItemBuilder(Material.DIAMOND_HELMET)
                .name(Component.text("❄ Корона Метели", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                .lore(
                        Component.text("Враги в радиусе 8 блоков", NamedTextColor.GRAY),
                        Component.text("получают Slowness II", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("✦ МИФИЧЕСКИЙ", NamedTextColor.LIGHT_PURPLE)
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
                .name(Component.text("❄ Кулак Мороза", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                .lore(
                        Component.text("Slowness III при ударе — 3 сек", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Дроп: Ледяной Голем", NamedTextColor.DARK_GRAY),
                        Component.empty(),
                        Component.text("✦ ЛЕГЕНДАРНЫЙ", NamedTextColor.GOLD)
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
                .name(Component.text("❄ Лук Вьюги", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                .lore(
                        Component.text("Slowness II при попадании — 4 сек", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Дроп: Королева Метели", NamedTextColor.DARK_GRAY),
                        Component.empty(),
                        Component.text("✦ ЛЕГЕНДАРНЫЙ", NamedTextColor.GOLD)
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
                .name(Component.text("🎁 Мешок Санты", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .lore(
                        Component.text("Содержит случайные ценности", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Дроп: Тёмный Санта", NamedTextColor.DARK_GRAY),
                        Component.empty(),
                        Component.text("✦ ЛЕГЕНДАРНЫЙ", NamedTextColor.GOLD)
                )
                .persistentData(key("frost_item"), "santa_bag")
                .glowing()
                .build();
    }

    // ============ Проверка кастомного предмета ============
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

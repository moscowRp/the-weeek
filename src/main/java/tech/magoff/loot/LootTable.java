package tech.magoff.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootTable {

    private static final Random RANDOM = new Random();

    /**
     * Генерирует содержимое сундука ивента.
     * Всегда содержит 1 Осколок Вечной Зимы + 3-6 рандомных предметов.
     */
    public static List<ItemStack> generateChestLoot() {
        List<ItemStack> loot = new ArrayList<>();

        // Всегда — Осколок Вечной Зимы
        loot.add(FrostItems.frostShard());

        // 3-6 рандомных предметов
        int count = 3 + RANDOM.nextInt(4);
        for (int i = 0; i < count; i++) {
            loot.add(rollItem());
        }

        return loot;
    }

    /**
     * Рандомный предмет по рарности:
     * 60% обычный, 30% редкий, 10% легендарный
     */
    private static ItemStack rollItem() {
        int roll = RANDOM.nextInt(100);

        if (roll < 60) {
            return rollCommon();
        } else if (roll < 90) {
            return rollRare();
        } else {
            return rollLegendary();
        }
    }

    // ============ Обычный (60%) ============
    private static ItemStack rollCommon() {
        return switch (RANDOM.nextInt(5)) {
            case 0 -> new ItemStack(Material.DIAMOND, 3 + RANDOM.nextInt(6));
            case 1 -> new ItemStack(Material.GOLDEN_APPLE, 2 + RANDOM.nextInt(3));
            case 2 -> new ItemStack(Material.IRON_BLOCK, 4 + RANDOM.nextInt(8));
            case 3 -> new ItemStack(Material.EXPERIENCE_BOTTLE, 16 + RANDOM.nextInt(16));
            default -> new ItemStack(Material.EMERALD, 5 + RANDOM.nextInt(10));
        };
    }

    // ============ Редкий (30%) ============
    private static ItemStack rollRare() {
        return switch (RANDOM.nextInt(5)) {
            case 0 -> new ItemStack(Material.NETHERITE_INGOT, 1);
            case 1 -> new ItemStack(Material.TOTEM_OF_UNDYING, 1);
            case 2 -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
            case 3 -> new ItemStack(Material.DIAMOND_BLOCK, 2 + RANDOM.nextInt(3));
            default -> new ItemStack(Material.NETHERITE_SCRAP, 2 + RANDOM.nextInt(3));
        };
    }

    // ============ Легендарный (10%) ============
    private static ItemStack rollLegendary() {
        return switch (RANDOM.nextInt(3)) {
            case 0 -> FrostItems.fistOfFrost();
            case 1 -> FrostItems.blizzardBow();
            default -> FrostItems.blizzardCrown();
        };
    }

    /**
     * Лут за убийство босса (помимо уникального дропа).
     */
    public static List<ItemStack> generateBossLoot(String bossType) {
        List<ItemStack> loot = new ArrayList<>();

        // Базовый лут
        loot.add(new ItemStack(Material.DIAMOND, 3 + RANDOM.nextInt(5)));
        loot.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 8 + RANDOM.nextInt(16)));

        // Уникальный дроп босса (30% шанс)
        if (RANDOM.nextInt(100) < 30) {
            switch (bossType) {
                case "frost_golem" -> loot.add(FrostItems.fistOfFrost());
                case "dark_santa" -> loot.add(FrostItems.santaBag());
                case "blizzard_queen" -> loot.add(FrostItems.blizzardBow());
            }
        }

        return loot;
    }
}

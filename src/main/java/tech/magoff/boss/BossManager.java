package tech.magoff.boss;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import tech.magoff.loot.LootTable;
import tech.magoff.theweeek;

import java.util.*;

public class BossManager {

    private final theweeek plugin;
    private final Map<UUID, Object> activeBosses = new HashMap<>(); // UUID → boss instance

    public BossManager(theweeek plugin) {
        this.plugin = plugin;
    }

    /**
     * Спавнит случайного босса рядом с игроком.
     */
    public void spawnRandomBoss(Location location) {
        Random random = new Random();
        int type = random.nextInt(3);

        switch (type) {
            case 0 -> spawnFrostGolem(location);
            case 1 -> spawnDarkSanta(location);
            case 2 -> spawnBlizzardQueen(location);
        }
    }

    public void spawnFrostGolem(Location location) {
        FrostGolem boss = FrostGolem.spawn(location);
        activeBosses.put(boss.getEntity().getUniqueId(), boss);

        announceNearby(location, Component.text("❄ Ледяной Голем пробудился!", NamedTextColor.AQUA));
        plugin.getLogger().info("Ледяной Голем заспавнен на " + formatLoc(location));
    }

    public void spawnDarkSanta(Location location) {
        DarkSanta boss = DarkSanta.spawn(location);
        activeBosses.put(boss.getEntity().getUniqueId(), boss);

        announceNearby(location, Component.text("🎅 Тёмный Санта появился!", NamedTextColor.DARK_RED));
        plugin.getLogger().info("Тёмный Санта заспавнен на " + formatLoc(location));
    }

    public void spawnBlizzardQueen(Location location) {
        BlizzardQueen boss = BlizzardQueen.spawn(location);
        activeBosses.put(boss.getEntity().getUniqueId(), boss);

        announceNearby(location, Component.text("🌨 Королева Метели появилась!", NamedTextColor.DARK_AQUA));
        plugin.getLogger().info("Королева Метели заспавнена на " + formatLoc(location));
    }

    /**
     * Проверяет, является ли моб боссом.
     */
    public boolean isBoss(UUID entityId) {
        return activeBosses.containsKey(entityId);
    }

    /**
     * Получить тип босса по UUID сущности.
     */
    public String getBossType(UUID entityId) {
        Object boss = activeBosses.get(entityId);
        if (boss instanceof FrostGolem) return "frost_golem";
        if (boss instanceof DarkSanta) return "dark_santa";
        if (boss instanceof BlizzardQueen) return "blizzard_queen";
        return null;
    }

    /**
     * Вызвать при смерти босса — дропает лут.
     */
    public void onBossDeath(UUID entityId, Location deathLocation) {
        Object boss = activeBosses.remove(entityId);
        if (boss == null) return;

        String bossType = null;
        String bossName = "";

        if (boss instanceof FrostGolem fg) {
            fg.cleanup();
            bossType = "frost_golem";
            bossName = "Ледяной Голем";
        } else if (boss instanceof DarkSanta ds) {
            ds.cleanup();
            bossType = "dark_santa";
            bossName = "Тёмный Санта";
        } else if (boss instanceof BlizzardQueen bq) {
            bq.cleanup();
            bossType = "blizzard_queen";
            bossName = "Королева Метели";
        }

        // Дроп лута
        if (bossType != null) {
            var loot = LootTable.generateBossLoot(bossType);
            for (var item : loot) {
                deathLocation.getWorld().dropItemNaturally(deathLocation, item);
            }

            // Опыт
            deathLocation.getWorld().spawn(deathLocation, org.bukkit.entity.ExperienceOrb.class, orb -> {
                orb.setExperience(500 + new Random().nextInt(500));
            });
        }

        // Объявление
        String finalBossName = bossName;
        for (Player p : deathLocation.getWorld().getPlayers()) {
            p.sendMessage(Component.text("☠ " + finalBossName + " повержен!", NamedTextColor.GOLD));
        }
    }

    /**
     * Очищает всех боссов (при остановке ивента).
     */
    public void cleanupAll() {
        for (Object boss : activeBosses.values()) {
            if (boss instanceof FrostGolem fg) fg.cleanup();
            else if (boss instanceof DarkSanta ds) ds.cleanup();
            else if (boss instanceof BlizzardQueen bq) bq.cleanup();
        }
        activeBosses.clear();
    }

    /**
     * Проверяет, все ли боссы мертвы.
     */
    public boolean allBossesDead() {
        activeBosses.entrySet().removeIf(entry -> {
            Object boss = entry.getValue();
            if (boss instanceof FrostGolem fg) return fg.isDead();
            if (boss instanceof DarkSanta ds) return ds.isDead();
            if (boss instanceof BlizzardQueen bq) return bq.isDead();
            return true;
        });
        return activeBosses.isEmpty();
    }

    public int getActiveBossCount() {
        return activeBosses.size();
    }

    private void announceNearby(Location loc, Component message) {
        for (Player p : loc.getWorld().getNearbyPlayers(loc, 100)) {
            p.sendMessage(message);
        }
    }

    private String formatLoc(Location loc) {
        return String.format("(%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}

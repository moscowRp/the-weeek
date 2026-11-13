package tech.magoff.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import tech.magoff.boss.BossManager;
import tech.magoff.theweeek;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FrostSiegeEvent {

    private final theweeek plugin;
    private final BossManager bossManager;
    private FrostChest frostChest;

    private boolean active = false;
    private BukkitRunnable mainTask;
    private int tickCounter = 0;
    private final int maxDuration = 1200; // 20 мин в секундах

    // Сохраняем оригинальное состояние мира
    private boolean originalStorm;
    private boolean originalThundering;
    private long originalTime;

    public FrostSiegeEvent(theweeek plugin, BossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Запуск ивента.
     */
    public void start() {
        if (active) return;
        active = true;
        tickCounter = 0;

        World world = Bukkit.getWorlds().get(0); // Основной мир

        // Сохраняем состояние
        originalStorm = world.hasStorm();
        originalThundering = world.isThundering();
        originalTime = world.getTime();

        // Фаза предупреждения
        announceAll(Component.text("═══════════════════════════════", NamedTextColor.DARK_AQUA));
        announceAll(Component.text("❄ ВЕЧНАЯ ЗИМА НАДВИГАЕТСЯ...", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        announceAll(Component.text("Готовьтесь! Ивент начнётся через 30 секунд!", NamedTextColor.GRAY));
        announceAll(Component.text("═══════════════════════════════", NamedTextColor.DARK_AQUA));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7f, 0.5f);
        }

        // Через 30 сек — начало
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) return;
                beginEvent(world);
            }
        }.runTaskLater(plugin, 600L); // 30 сек
    }

    private void beginEvent(World world) {
        // Погода
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(maxDuration * 20);
        world.setTime(18000); // Ночь

        announceAll(Component.text("❄ ВЕЧНАЯ ЗИМА НАЧАЛАСЬ!", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));

        // Спавн сундука
        frostChest = new FrostChest(plugin);
        frostChest.spawn(world);

        // Спавн боссов через 10 сек после начала
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) return;
                spawnBosses(world);
            }
        }.runTaskLater(plugin, 200L); // 10 сек

        // Основной тикер ивента
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) {
                    cancel();
                    return;
                }

                tickCounter++;

                // Глобальные эффекты каждые 10 сек
                if (tickCounter % 10 == 0) {
                    applyGlobalEffects(world);
                }

                // Спавн усиленных мобов каждые 45 сек
                if (tickCounter % 45 == 0) {
                    spawnWinterMobs(world);
                }

                // Держать ночь
                world.setTime(18000);

                // Проверка условий окончания
                if (checkEndConditions()) {
                    endEvent(true);
                    cancel();
                    return;
                }

                // Таймаут
                if (tickCounter >= maxDuration) {
                    endEvent(false);
                    cancel();
                }

                // Объявления каждые 5 минут
                if (tickCounter % 300 == 0) {
                    int minutesLeft = (maxDuration - tickCounter) / 60;
                    announceAll(Component.text("❄ Вечная Зима: осталось " + minutesLeft + " мин. Боссов в живых: "
                            + bossManager.getActiveBossCount(), NamedTextColor.AQUA));
                }
            }
        };
        mainTask.runTaskTimer(plugin, 20L, 20L); // каждую секунду
    }

    private void spawnBosses(World world) {
        List<Player> players = new ArrayList<>(world.getPlayers());
        if (players.isEmpty()) return;

        Collections.shuffle(players);
        Random random = new Random();

        // Спавним 1-3 босса (зависит от кол-ва игроков)
        int bossCount = Math.min(3, Math.max(1, players.size() / 3));

        for (int i = 0; i < bossCount; i++) {
            Player target = players.get(i % players.size());
            Location spawnLoc = target.getLocation().add(
                    (random.nextDouble() - 0.5) * 40,
                    0,
                    (random.nextDouble() - 0.5) * 40
            );
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);

            bossManager.spawnRandomBoss(spawnLoc);
        }
    }

    private void applyGlobalEffects(World world) {
        for (Player p : world.getPlayers()) {
            // Slowness I от холода (если нет иммунитета)
            if (!hasWinterImmunity(p)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 220, 0, true, false));
            }

            // Частицы снега
            p.spawnParticle(Particle.SNOWFLAKE, p.getLocation().add(0, 3, 0), 20, 5, 2, 5, 0.01);
        }
    }

    private boolean hasWinterImmunity(Player player) {
        // Проверка Сердца Зимы в инвентаре
        for (var item : player.getInventory().getContents()) {
            if (tech.magoff.loot.FrostItems.isFrostItem(item, "heart_of_winter")) {
                return true;
            }
        }
        return false;
    }

    private void spawnWinterMobs(World world) {
        Random random = new Random();
        for (Player p : world.getPlayers()) {
            // 2-3 моба рядом с каждым игроком
            int count = 2 + random.nextInt(2);
            for (int i = 0; i < count; i++) {
                Location spawnLoc = p.getLocation().add(
                        (random.nextDouble() - 0.5) * 30,
                        0,
                        (random.nextDouble() - 0.5) * 30
                );
                spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);

                if (random.nextBoolean()) {
                    world.spawn(spawnLoc, org.bukkit.entity.Stray.class, stray -> {
                        stray.customName(Component.text("Зимний Скелет", NamedTextColor.AQUA));
                        stray.setCustomNameVisible(true);
                        stray.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_HELMET));
                    });
                } else {
                    world.spawn(spawnLoc, org.bukkit.entity.Skeleton.class, skel -> {
                        skel.customName(Component.text("Ледяной Стрелок", NamedTextColor.AQUA));
                        skel.setCustomNameVisible(true);
                        skel.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.IRON_HELMET));
                        skel.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.BOW));
                    });
                }
            }
        }
    }

    private boolean checkEndConditions() {
        boolean bossesDefeated = bossManager.allBossesDead();
        boolean chestLooted = frostChest != null && frostChest.isLooted();
        return bossesDefeated && chestLooted;
    }

    /**
     * Окончание ивента.
     */
    public void endEvent(boolean victory) {
        if (!active) return;
        active = false;

        World world = Bukkit.getWorlds().get(0);

        // Восстановить мир
        world.setStorm(originalStorm);
        world.setThundering(originalThundering);
        world.setTime(originalTime);

        // Остановить таски
        if (mainTask != null && !mainTask.isCancelled()) {
            mainTask.cancel();
        }

        // Очистить боссов
        bossManager.cleanupAll();

        // Очистить сундук (если не забран)
        if (frostChest != null) {
            frostChest.cleanup();
        }

        // Объявление
        if (victory) {
            announceAll(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
            announceAll(Component.text("🏆 ВЕЧНАЯ ЗИМА ПОБЕЖДЕНА!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            announceAll(Component.text("Все боссы повержены! Мир спасён!", NamedTextColor.YELLOW));
            announceAll(Component.text("═══════════════════════════════", NamedTextColor.GOLD));

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        } else {
            announceAll(Component.text("═══════════════════════════════", NamedTextColor.RED));
            announceAll(Component.text("💀 ВЕЧНАЯ ЗИМА ПОБЕДИЛА!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
            announceAll(Component.text("Время вышло... Зима отступает, но вернётся.", NamedTextColor.GRAY));
            announceAll(Component.text("═══════════════════════════════", NamedTextColor.RED));

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.7f, 0.5f);
            }
        }
    }

    /**
     * Принудительная остановка (команда админа).
     */
    public void forceStop() {
        endEvent(false);
    }

    public FrostChest getFrostChest() {
        return frostChest;
    }

    private void announceAll(Component message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(message);
        }
    }
}

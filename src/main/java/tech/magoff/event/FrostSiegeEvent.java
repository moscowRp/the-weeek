package tech.magoff.event;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import tech.magoff.boss.BossManager;
import tech.magoff.theweeek;
import tech.magoff.util.TextUtil;

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
    private final int maxDuration = 1200;

    private boolean originalStorm;
    private boolean originalThundering;

    public FrostSiegeEvent(theweeek plugin, BossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        if (active) return;
        active = true;
        tickCounter = 0;

        World world = Bukkit.getWorlds().get(0);

        originalStorm = world.hasStorm();
        originalThundering = world.isThundering();

        announceAll(Component.empty());
        announceAll(TextUtil.parse("<dark_gray>╭────────────────────────────────────────╮</dark_gray>"));
        announceAll(TextUtil.parse("   <gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("ВЕЧНАЯ ЗИМА НАДВИГАЕТСЯ...") + "</b></gradient>"));
        announceAll(TextUtil.parse("   <gray>" + TextUtil.toSmallCaps("Холодный фронт охватит мир через 30 секунд!") + "</gray>"));
        announceAll(TextUtil.parse("   <gray>" + TextUtil.toSmallCaps("Приготовьте снаряжение к ледяной осаде.") + "</gray>"));
        announceAll(TextUtil.parse("<dark_gray>╰────────────────────────────────────────╯</dark_gray>"));
        announceAll(Component.empty());

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7f, 0.5f);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) return;
                beginEvent(world);
            }
        }.runTaskLater(plugin, 600L);
    }

    private void beginEvent(World world) {
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(maxDuration * 20);

        announceAll(Component.empty());
        announceAll(TextUtil.parse("<gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("ВЕЧНАЯ ЗИМА ОХВАТИЛА МИР!") + "</b></gradient>"));
        announceAll(TextUtil.parse("<gray>" + TextUtil.toSmallCaps("Ищите сундук и уничтожайте боссов, пока не поздно!") + "</gray>"));
        announceAll(Component.empty());

        frostChest = new FrostChest(plugin);
        frostChest.spawn(world);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) return;
                spawnBosses(world);
            }
        }.runTaskLater(plugin, 200L);

        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) {
                    cancel();
                    return;
                }

                tickCounter++;

                if (tickCounter % 10 == 0) {
                    applyGlobalEffects(world);
                }

                if (tickCounter % 45 == 0) {
                    spawnWinterMobs(world);
                }

                if (checkEndConditions()) {
                    endEvent(true);
                    cancel();
                    return;
                }

                if (tickCounter >= maxDuration) {
                    endEvent(false);
                    cancel();
                }

                if (tickCounter % 300 == 0) {
                    int minutesLeft = (maxDuration - tickCounter) / 60;
                    announceAll(TextUtil.parse(TextUtil.prefix() + "<gradient:#00f2fe:#4facfe>" + TextUtil.toSmallCaps("До конца Зимы:") + "</gradient> <#ffd200><b>" + minutesLeft + " " + TextUtil.toSmallCaps("мин") + "</b></#ffd200> <dark_gray>•</dark_gray> <gray>" + TextUtil.toSmallCaps("Боссов в живых:") + "</gray> <#ff4b2b><b>" + bossManager.getActiveBossCount() + "</b></#ff4b2b>"));
                }
            }
        };
        mainTask.runTaskTimer(plugin, 20L, 20L);
    }

    private void spawnBosses(World world) {
        List<Player> players = new ArrayList<>(world.getPlayers());
        if (players.isEmpty()) return;

        Collections.shuffle(players);
        Random random = new Random();

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
            if (!hasWinterImmunity(p)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 220, 0, true, false));
            }
            p.spawnParticle(Particle.SNOWFLAKE, p.getLocation().add(0, 3, 0), 20, 5, 2, 5, 0.01);
        }
    }

    private boolean hasWinterImmunity(Player player) {
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
                        stray.customName(TextUtil.parse("<#70a1ff>" + TextUtil.toSmallCaps("Зимний Скелет") + "</#70a1ff>"));
                        stray.setCustomNameVisible(true);
                        stray.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_HELMET));
                    });
                } else {
                    world.spawn(spawnLoc, org.bukkit.entity.Skeleton.class, skel -> {
                        skel.customName(TextUtil.parse("<#70a1ff>" + TextUtil.toSmallCaps("Ледяной Стрелок") + "</#70a1ff>"));
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

    public void endEvent(boolean victory) {
        if (!active) return;
        active = false;

        World world = Bukkit.getWorlds().get(0);

        world.setStorm(originalStorm);
        world.setThundering(originalThundering);

        if (mainTask != null && !mainTask.isCancelled()) {
            mainTask.cancel();
        }

        bossManager.cleanupAll();

        if (frostChest != null) {
            frostChest.cleanup();
        }

        if (victory) {
            announceAll(Component.empty());
            announceAll(TextUtil.parse("<dark_gray>╭────────────────────────────────────────╮</dark_gray>"));
            announceAll(TextUtil.parse("   <gradient:#43e97b:#38f9d7><b>🏆 " + TextUtil.toSmallCaps("ВЕЧНАЯ ЗИМА ОТСТУПИЛА!") + "</b></gradient>"));
            announceAll(TextUtil.parse("   <gray>" + TextUtil.toSmallCaps("Все ледяные боссы повержены, а сундук открыт!") + "</gray>"));
            announceAll(TextUtil.parse("   <gradient:#ffd200:#f7971e><b>" + TextUtil.toSmallCaps("Мир спасен от вечной мерзлоты!") + "</b></gradient>"));
            announceAll(TextUtil.parse("<dark_gray>╰────────────────────────────────────────╯</dark_gray>"));
            announceAll(Component.empty());

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        } else {
            announceAll(Component.empty());
            announceAll(TextUtil.parse("<dark_gray>╭────────────────────────────────────────╮</dark_gray>"));
            announceAll(TextUtil.parse("   <gradient:#ff416c:#ff4b2b><b>💀 " + TextUtil.toSmallCaps("ВЕЧНАЯ ЗИМА ПОБЕДИЛА") + "</b></gradient>"));
            announceAll(TextUtil.parse("   <gray>" + TextUtil.toSmallCaps("Время вышло... Мороз отступает, но он вернется.") + "</gray>"));
            announceAll(TextUtil.parse("<dark_gray>╰────────────────────────────────────────╯</dark_gray>"));
            announceAll(Component.empty());

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.7f, 0.5f);
            }
        }
    }

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

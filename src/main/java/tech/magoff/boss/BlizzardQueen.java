package tech.magoff.boss;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import tech.magoff.theweeek;
import tech.magoff.util.TextUtil;

import java.util.*;

public class BlizzardQueen {

    private final theweeek plugin;
    private final Stray entity;
    private final BossBar bossBar;
    private BukkitRunnable abilityTask;
    private boolean phaseTriggered = false;
    private final List<ArmorStand> decoys = new ArrayList<>();

    public BlizzardQueen(theweeek plugin, Stray entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.bossBar = BossBar.bossBar(
                TextUtil.parse("<gradient:#b176ff:#e0c3fc><b>🌨 " + TextUtil.toSmallCaps("Королева Метели") + "</b></gradient>"),
                1.0f,
                BossBar.Color.PURPLE,
                BossBar.Overlay.NOTCHED_10
        );
    }

    public static BlizzardQueen spawn(Location location) {
        theweeek plugin = theweeek.getInstance();
        World world = location.getWorld();

        Stray stray = world.spawn(location, Stray.class, e -> {
            e.customName(TextUtil.parse("<gradient:#b176ff:#e0c3fc><b>🌨 " + TextUtil.toSmallCaps("Королева Метели") + "</b></gradient>"));
            e.setCustomNameVisible(true);

            Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(350);
            e.setHealth(350);
            Objects.requireNonNull(e.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.35);
            Objects.requireNonNull(e.getAttribute(Attribute.ARMOR)).setBaseValue(15);

            e.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            e.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            e.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            e.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            e.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));

            e.getEquipment().setHelmetDropChance(0);
            e.getEquipment().setChestplateDropChance(0);
            e.getEquipment().setLeggingsDropChance(0);
            e.getEquipment().setBootsDropChance(0);
            e.getEquipment().setItemInMainHandDropChance(0);

            e.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "boss_type"),
                    PersistentDataType.STRING,
                    "blizzard_queen"
            );

            e.setRemoveWhenFarAway(false);
            e.setPersistent(true);
        });

        BlizzardQueen boss = new BlizzardQueen(plugin, stray);
        boss.startAbilities();
        return boss;
    }

    private void startAbilities() {
        abilityTask = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (entity.isDead() || !entity.isValid()) {
                    cleanupDecoys();
                    cleanup();
                    cancel();
                    return;
                }

                tick++;
                updateBossBar();

                if (tick % 2 == 0) {
                    retarget();
                }

                if (tick % 15 == 0) {
                    blizzard();
                }

                if (tick % 10 == 0) {
                    iceSpears();
                }

                if (!phaseTriggered && entity.getHealth() <= 175) {
                    stormPhase();
                    phaseTriggered = true;
                }
            }
        };
        abilityTask.runTaskTimer(plugin, 20L, 20L);
    }

    private void updateBossBar() {
        double maxHealth = Objects.requireNonNull(entity.getAttribute(Attribute.MAX_HEALTH)).getValue();
        float progress = (float) Math.max(0.0, Math.min(1.0, entity.getHealth() / maxHealth));
        bossBar.progress(progress);

        int currentHp = (int) Math.ceil(entity.getHealth());
        bossBar.name(TextUtil.parse("<gradient:#b176ff:#e0c3fc><b>🌨 " + TextUtil.toSmallCaps("Королева Метели") + "</b></gradient> " +
                "<dark_gray>[</dark_gray><#ff5858>" + currentHp + "</#ff5858><gray>/</gray><#ff5858>" + (int)maxHealth + " ❤</#ff5858><dark_gray>]</dark_gray>"));

        Location loc = entity.getLocation();
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(loc) <= 3600) {
                p.showBossBar(bossBar);
            } else {
                p.hideBossBar(bossBar);
            }
        }
    }

    private void retarget() {
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : entity.getLocation().getWorld().getNearbyPlayers(entity.getLocation(), 50)) {
            double dist = p.getLocation().distanceSquared(entity.getLocation());
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }
        if (nearest != null) {
            entity.setTarget(nearest);
        }
    }

    private void blizzard() {
        Location loc = entity.getLocation();
        World world = loc.getWorld();

        for (Player p : world.getNearbyPlayers(loc, 30)) {
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#b176ff:#e0c3fc>" + TextUtil.toSmallCaps("Королева Метели вызывает снежный буран!") + "</gradient>"));
        }

        for (Player p : world.getNearbyPlayers(loc, 12)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
            p.damage(4.0, entity);

            int currentFreeze = p.getFreezeTicks();
            p.setFreezeTicks(Math.min(currentFreeze + 100, p.getMaxFreezeTicks()));
        }

        world.spawnParticle(Particle.SNOWFLAKE, loc, 200, 12, 4, 12, 0.05);
        world.spawnParticle(Particle.CLOUD, loc, 100, 10, 3, 10, 0.02);
        world.playSound(loc, Sound.WEATHER_RAIN, 2f, 0.3f);
    }

    private void iceSpears() {
        Location loc = entity.getLocation().add(0, 1.5, 0);
        World world = loc.getWorld();

        Player target = (Player) entity.getTarget();
        if (target == null) return;

        Vector direction = target.getLocation().toVector().subtract(loc.toVector()).normalize();

        for (int i = -2; i <= 2; i++) {
            Vector spread = direction.clone().rotateAroundY(Math.toRadians(i * 12)).multiply(2.5);
            spread.setY(direction.getY() + 0.1);

            Arrow arrow = world.spawn(loc, Arrow.class);
            arrow.setVelocity(spread);
            arrow.setShooter(entity);
            arrow.setDamage(6.0);
            arrow.setColor(Color.fromRGB(150, 220, 255));
            arrow.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2), true);
            arrow.setGlowing(true);
        }

        world.playSound(loc, Sound.ENTITY_ARROW_SHOOT, 2f, 0.5f);
        world.spawnParticle(Particle.SNOWFLAKE, loc, 30, 1, 1, 1, 0.1);
    }

    private void stormPhase() {
        Location loc = entity.getLocation();
        World world = loc.getWorld();

        for (Player p : world.getNearbyPlayers(loc, 30)) {
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff416c:#ff4b2b><b>" + TextUtil.toSmallCaps("Королева Метели входит в фазу шторма!") + "</b></gradient>"));
        }

        for (int i = 0; i < 3; i++) {
            Location decoyLoc = loc.clone().add(
                    (Math.random() - 0.5) * 16, 0, (Math.random() - 0.5) * 16
            );
            decoyLoc.setY(world.getHighestBlockYAt(decoyLoc) + 1);

            ArmorStand decoy = world.spawn(decoyLoc, ArmorStand.class, as -> {
                as.customName(TextUtil.parse("<gradient:#b176ff:#e0c3fc><b>🌨 " + TextUtil.toSmallCaps("Королева Метели") + "</b></gradient>"));
                as.setCustomNameVisible(true);
                as.setGravity(false);
                as.setVisible(true);
                as.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                as.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                as.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                as.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            });
            decoys.add(decoy);

            world.spawnParticle(Particle.SNOWFLAKE, decoyLoc, 50, 1, 2, 1, 0.1);
        }

        Location tpLoc = loc.clone().add(
                (Math.random() - 0.5) * 20, 0, (Math.random() - 0.5) * 20
        );
        tpLoc.setY(world.getHighestBlockYAt(tpLoc) + 1);
        entity.teleport(tpLoc);

        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));

        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupDecoys();
            }
        }.runTaskLater(plugin, 100L);

        world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0);
        world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 0.5f);
    }

    private void cleanupDecoys() {
        for (ArmorStand decoy : decoys) {
            if (decoy.isValid()) decoy.remove();
        }
        decoys.clear();
    }

    public void cleanup() {
        if (abilityTask != null && !abilityTask.isCancelled()) {
            abilityTask.cancel();
        }
        cleanupDecoys();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hideBossBar(bossBar);
        }
        if (!entity.isDead() && entity.isValid()) {
            entity.remove();
        }
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public boolean isDead() {
        return entity.isDead() || !entity.isValid();
    }
}

package tech.magoff.boss;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import tech.magoff.theweeek;

import java.util.Objects;

public class FrostGolem {

    private final theweeek plugin;
    private final IronGolem entity;
    private BukkitRunnable abilityTask;
    private boolean phaseTriggered = false;

    public FrostGolem(theweeek plugin, IronGolem entity) {
        this.plugin = plugin;
        this.entity = entity;
    }

    public static FrostGolem spawn(Location location) {
        theweeek plugin = theweeek.getInstance();
        World world = location.getWorld();

        IronGolem golem = world.spawn(location, IronGolem.class, e -> {
            e.customName(Component.text("❄ Ледяной Голем", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
            e.setCustomNameVisible(true);
            e.setPlayerCreated(false);

            Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(500);
            e.setHealth(500);
            Objects.requireNonNull(e.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(15);
            Objects.requireNonNull(e.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.3);
            Objects.requireNonNull(e.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).setBaseValue(0.8);

            e.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "boss_type"),
                    PersistentDataType.STRING,
                    "frost_golem"
            );

            e.setRemoveWhenFarAway(false);
            e.setPersistent(true);
        });

        FrostGolem boss = new FrostGolem(plugin, golem);
        boss.startAbilities();
        return boss;
    }

    private void startAbilities() {
        abilityTask = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (entity.isDead() || !entity.isValid()) {
                    cancel();
                    return;
                }

                tick++;

                // Таргетить ближайшего игрока
                if (tick % 2 == 0) {
                    retarget();
                }

                // Морозный Шторм — каждые 20 сек
                if (tick % 20 == 0) {
                    frostStorm();
                }

                // AoE замедление — каждые 5 сек
                if (tick % 5 == 0) {
                    iceSlam();
                }

                // Фаза щита — при 30% HP
                if (!phaseTriggered && entity.getHealth() <= 150) {
                    iceShield();
                    phaseTriggered = true;
                }
            }
        };
        abilityTask.runTaskTimer(plugin, 20L, 20L); // каждую секунду
    }

    private void retarget() {
        Player nearest = null;
        double minDist = 50;
        for (Player p : entity.getLocation().getWorld().getNearbyPlayers(entity.getLocation(), 50)) {
            double dist = p.getLocation().distanceSquared(entity.getLocation());
            if (dist < minDist * minDist) {
                minDist = Math.sqrt(dist);
                nearest = p;
            }
        }
        if (nearest != null) {
            entity.setTarget(nearest);
        }
    }

    private void frostStorm() {
        Location loc = entity.getLocation().add(0, 1.5, 0);
        World world = loc.getWorld();

        for (Player p : world.getNearbyPlayers(loc, 30)) {
            p.sendMessage(Component.text("❄ Ледяной Голем вызывает Морозный Шторм!", NamedTextColor.AQUA));
        }

        // Кольцо снежков
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            Vector direction = new Vector(Math.cos(rad), 0.3, Math.sin(rad)).normalize().multiply(1.5);
            Snowball snowball = world.spawn(loc, Snowball.class);
            snowball.setVelocity(direction);
            snowball.setShooter(entity);
        }

        world.spawnParticle(Particle.SNOWFLAKE, loc, 100, 3, 2, 3, 0.1);
        world.playSound(loc, Sound.ENTITY_SNOW_GOLEM_HURT, 2f, 0.5f);
    }

    private void iceSlam() {
        Location loc = entity.getLocation();
        World world = loc.getWorld();

        for (Player player : world.getNearbyPlayers(loc, 8)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
            Vector knockback = player.getLocation().toVector()
                    .subtract(loc.toVector()).normalize().multiply(1.2);
            knockback.setY(0.4);
            player.setVelocity(knockback);
        }

        world.spawnParticle(Particle.SNOWFLAKE, loc, 50, 4, 1, 4, 0.05);
        world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.5f, 0.5f);
    }

    private void iceShield() {
        Location loc = entity.getLocation();
        World world = loc.getWorld();

        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 1));

        for (Player p : world.getNearbyPlayers(loc, 30)) {
            p.sendMessage(Component.text("❄ Ледяной Голем активирует Ледяной Щит!", NamedTextColor.RED));
        }

        // Спавн мини-големов
        for (int i = 0; i < 4; i++) {
            Location spawnLoc = loc.clone().add(
                    (Math.random() - 0.5) * 6, 0, (Math.random() - 0.5) * 6
            );
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);
            world.spawn(spawnLoc, Snowman.class, snowman -> {
                snowman.customName(Component.text("Мини-Голем", NamedTextColor.WHITE));
                snowman.setCustomNameVisible(true);
                Objects.requireNonNull(snowman.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(40);
                snowman.setHealth(40);
            });
        }

        world.spawnParticle(Particle.END_ROD, loc, 200, 5, 3, 5, 0.1);
        world.playSound(loc, Sound.BLOCK_ANVIL_LAND, 2f, 0.3f);
    }

    public void cleanup() {
        if (abilityTask != null && !abilityTask.isCancelled()) {
            abilityTask.cancel();
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

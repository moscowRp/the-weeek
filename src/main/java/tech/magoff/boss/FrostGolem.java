package tech.magoff.boss;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import tech.magoff.theweeek;
import tech.magoff.util.TextUtil;

import java.util.Objects;

public class FrostGolem {

    private final theweeek plugin;
    private final IronGolem entity;
    private final BossBar bossBar;
    private BukkitRunnable abilityTask;
    private boolean phaseTriggered = false;

    public FrostGolem(theweeek plugin, IronGolem entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.bossBar = BossBar.bossBar(
                TextUtil.parse("<gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("Ледяной Голем") + "</b></gradient>"),
                1.0f,
                BossBar.Color.BLUE,
                BossBar.Overlay.NOTCHED_10
        );
    }

    public static FrostGolem spawn(Location location) {
        theweeek plugin = theweeek.getInstance();
        World world = location.getWorld();

        IronGolem golem = world.spawn(location, IronGolem.class, e -> {
            e.customName(TextUtil.parse("<gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("Ледяной Голем") + "</b></gradient>"));
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
                    cleanup();
                    cancel();
                    return;
                }

                tick++;
                updateBossBar();

                if (tick % 2 == 0) {
                    retarget();
                }

                if (tick % 20 == 0) {
                    frostStorm();
                }

                if (tick % 5 == 0) {
                    iceSlam();
                }

                if (!phaseTriggered && entity.getHealth() <= 150) {
                    iceShield();
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
        bossBar.name(TextUtil.parse("<gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("Ледяной Голем") + "</b></gradient> " +
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
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#00f2fe:#4facfe>" + TextUtil.toSmallCaps("Ледяной Голем вызывает Морозный Шторм!") + "</gradient>"));
        }

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
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff416c:#ff4b2b><b>" + TextUtil.toSmallCaps("Ледяной Голем активирует Ледяной Щит!") + "</b></gradient>"));
        }

        for (int i = 0; i < 4; i++) {
            Location spawnLoc = loc.clone().add(
                    (Math.random() - 0.5) * 6, 0, (Math.random() - 0.5) * 6
            );
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);
            world.spawn(spawnLoc, Snowman.class, snowman -> {
                snowman.customName(TextUtil.parse("<white>" + TextUtil.toSmallCaps("Мини-Голем") + "</white>"));
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

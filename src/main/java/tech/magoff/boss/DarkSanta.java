package tech.magoff.boss;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import tech.magoff.theweeek;
import tech.magoff.util.TextUtil;

import java.util.*;

public class DarkSanta {

    private final theweeek plugin;
    private final Evoker entity;
    private final BossBar bossBar;
    private BukkitRunnable abilityTask;
    private final List<ArmorStand> trapGifts = new ArrayList<>();

    private static final PotionEffectType[] CHAOS_EFFECTS = {
            PotionEffectType.BLINDNESS,
            PotionEffectType.POISON,
            PotionEffectType.LEVITATION,
            PotionEffectType.WEAKNESS,
            PotionEffectType.NAUSEA
    };

    public DarkSanta(theweeek plugin, Evoker entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.bossBar = BossBar.bossBar(
                TextUtil.parse("<gradient:#ff0844:#ffb199><b>🎅 " + TextUtil.toSmallCaps("Тёмный Санта") + "</b></gradient>"),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.NOTCHED_10
        );
    }

    public static DarkSanta spawn(Location location) {
        theweeek plugin = theweeek.getInstance();
        World world = location.getWorld();

        Evoker evoker = world.spawn(location, Evoker.class, e -> {
            e.customName(TextUtil.parse("<gradient:#ff0844:#ffb199><b>🎅 " + TextUtil.toSmallCaps("Тёмный Санта") + "</b></gradient>"));
            e.setCustomNameVisible(true);

            Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(400);
            e.setHealth(400);
            Objects.requireNonNull(e.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.35);

            ItemStack helmet = createColoredArmor(Material.LEATHER_HELMET, Color.RED);
            ItemStack chestplate = createColoredArmor(Material.LEATHER_CHESTPLATE, Color.RED);
            ItemStack leggings = createColoredArmor(Material.LEATHER_LEGGINGS, Color.RED);
            ItemStack boots = createColoredArmor(Material.LEATHER_BOOTS, Color.RED);

            e.getEquipment().setHelmet(helmet);
            e.getEquipment().setChestplate(chestplate);
            e.getEquipment().setLeggings(leggings);
            e.getEquipment().setBoots(boots);

            e.getEquipment().setHelmetDropChance(0);
            e.getEquipment().setChestplateDropChance(0);
            e.getEquipment().setLeggingsDropChance(0);
            e.getEquipment().setBootsDropChance(0);

            e.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "boss_type"),
                    PersistentDataType.STRING,
                    "dark_santa"
            );

            e.setRemoveWhenFarAway(false);
            e.setPersistent(true);
            e.setPatrolLeader(false);
        });

        DarkSanta boss = new DarkSanta(plugin, evoker);
        boss.startAbilities();
        return boss;
    }

    private static ItemStack createColoredArmor(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    private void startAbilities() {
        abilityTask = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (entity.isDead() || !entity.isValid()) {
                    cleanupGifts();
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
                    trapGift();
                }

                if (tick % 25 == 0) {
                    summonElves();
                }

                if (tick % 30 == 0) {
                    chaosBag();
                }

                checkTraps();
            }
        };
        abilityTask.runTaskTimer(plugin, 20L, 20L);
    }

    private void updateBossBar() {
        double maxHealth = Objects.requireNonNull(entity.getAttribute(Attribute.MAX_HEALTH)).getValue();
        float progress = (float) Math.max(0.0, Math.min(1.0, entity.getHealth() / maxHealth));
        bossBar.progress(progress);

        int currentHp = (int) Math.ceil(entity.getHealth());
        bossBar.name(TextUtil.parse("<gradient:#ff0844:#ffb199><b>🎅 " + TextUtil.toSmallCaps("Тёмный Санта") + "</b></gradient> " +
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

    private void trapGift() {
        Location loc = entity.getLocation();
        World world = loc.getWorld();

        Location giftLoc = loc.clone().add(
                (Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10
        );
        giftLoc.setY(world.getHighestBlockYAt(giftLoc) + 1);

        ArmorStand gift = world.spawn(giftLoc, ArmorStand.class, as -> {
            as.customName(TextUtil.parse("<gradient:#43e97b:#38f9d7><b>🎁 " + TextUtil.toSmallCaps("Подарок") + "</b></gradient>"));
            as.setCustomNameVisible(true);
            as.setVisible(false);
            as.setGravity(false);
            as.setSmall(true);
            as.setMarker(false);
            as.getEquipment().setHelmet(new ItemStack(Material.TNT));
            as.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "trap_gift"),
                    PersistentDataType.BOOLEAN,
                    true
            );
        });
        trapGifts.add(gift);

        for (Player p : world.getNearbyPlayers(loc, 30)) {
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff0844:#ffb199>" + TextUtil.toSmallCaps("Тёмный Санта бросает подозрительный подарок!") + "</gradient>"));
        }

        world.spawnParticle(Particle.HAPPY_VILLAGER, giftLoc, 20, 0.5, 0.5, 0.5, 0);
    }

    private void checkTraps() {
        Iterator<ArmorStand> it = trapGifts.iterator();
        while (it.hasNext()) {
            ArmorStand gift = it.next();
            if (!gift.isValid() || gift.isDead()) {
                it.remove();
                continue;
            }

            Location giftLoc = gift.getLocation();
            for (Player p : giftLoc.getWorld().getNearbyPlayers(giftLoc, 3)) {
                giftLoc.getWorld().createExplosion(giftLoc, 3f, false, false);
                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                p.damage(8.0, entity);
                gift.remove();
                it.remove();

                giftLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, giftLoc, 3, 0, 0, 0, 0);
                break;
            }
        }
    }

    private void summonElves() {
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        int count = 3 + new Random().nextInt(3);

        for (Player p : world.getNearbyPlayers(loc, 30)) {
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff0844:#ffb199>" + TextUtil.toSmallCaps("Тёмный Санта призывает злобных эльфов!") + "</gradient>"));
        }

        for (int i = 0; i < count; i++) {
            world.spawn(loc.clone().add(
                    (Math.random() - 0.5) * 4, 1, (Math.random() - 0.5) * 4
            ), Vex.class, vex -> {
                vex.customName(TextUtil.parse("<#2ed573>" + TextUtil.toSmallCaps("Злой Эльф") + "</#2ed573>"));
                vex.setCustomNameVisible(true);
                ItemStack greenHelmet = createColoredArmor(Material.LEATHER_HELMET, Color.GREEN);
                vex.getEquipment().setHelmet(greenHelmet);
                vex.setCharging(true);
            });
        }

        world.playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 2f, 1f);
        world.spawnParticle(Particle.WITCH, loc, 50, 2, 2, 2, 0.1);
    }

    private void chaosBag() {
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        Random random = new Random();

        PotionEffectType effect = CHAOS_EFFECTS[random.nextInt(CHAOS_EFFECTS.length)];

        for (Player p : world.getNearbyPlayers(loc, 20)) {
            p.addPotionEffect(new PotionEffect(effect, 60, 1));
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff0844:#ffb199>" + TextUtil.toSmallCaps("Мешок Хаоса!") + "</gradient> <gray>" + TextUtil.toSmallCaps("Вы получили эффект:") + "</gray> <#ffa502>" + formatEffectName(effect) + "</#ffa502>"));
        }

        world.spawnParticle(Particle.WITCH, loc, 80, 10, 3, 10, 0.1);
        world.playSound(loc, Sound.ENTITY_EVOKER_CELEBRATE, 2f, 0.5f);
    }

    private String formatEffectName(PotionEffectType type) {
        if (type == PotionEffectType.BLINDNESS) return TextUtil.toSmallCaps("Слепота");
        if (type == PotionEffectType.POISON) return TextUtil.toSmallCaps("Отравление");
        if (type == PotionEffectType.LEVITATION) return TextUtil.toSmallCaps("Левитация");
        if (type == PotionEffectType.WEAKNESS) return TextUtil.toSmallCaps("Слабость");
        if (type == PotionEffectType.NAUSEA) return TextUtil.toSmallCaps("Тошнота");
        return TextUtil.toSmallCaps(type.getKey().getKey());
    }

    private void cleanupGifts() {
        for (ArmorStand gift : trapGifts) {
            if (gift.isValid()) gift.remove();
        }
        trapGifts.clear();
    }

    public void cleanup() {
        if (abilityTask != null && !abilityTask.isCancelled()) {
            abilityTask.cancel();
        }
        cleanupGifts();
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

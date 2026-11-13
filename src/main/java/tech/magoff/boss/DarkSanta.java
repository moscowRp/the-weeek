package tech.magoff.boss;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

import java.util.*;

public class DarkSanta {

    private final theweeek plugin;
    private final Evoker entity;
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
    }

    public static DarkSanta spawn(Location location) {
        theweeek plugin = theweeek.getInstance();
        World world = location.getWorld();

        Evoker evoker = world.spawn(location, Evoker.class, e -> {
            e.customName(Component.text("🎅 Тёмный Санта", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
            e.setCustomNameVisible(true);

            Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(400);
            e.setHealth(400);
            Objects.requireNonNull(e.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.35);

            // Красная кожаная броня
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
                    cancel();
                    return;
                }

                tick++;

                // Таргетить ближайшего игрока
                if (tick % 2 == 0) {
                    retarget();
                }

                // Подарок-Ловушка — каждые 15 сек
                if (tick % 15 == 0) {
                    trapGift();
                }

                // Призыв Эльфов — каждые 25 сек
                if (tick % 25 == 0) {
                    summonElves();
                }

                // Мешок Хаоса — каждые 30 сек
                if (tick % 30 == 0) {
                    chaosBag();
                }

                // Проверка ловушек
                checkTraps();
            }
        };
        abilityTask.runTaskTimer(plugin, 20L, 20L);
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

        // Бросить "подарок" в случайную точку рядом
        Location giftLoc = loc.clone().add(
                (Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10
        );
        giftLoc.setY(world.getHighestBlockYAt(giftLoc) + 1);

        ArmorStand gift = world.spawn(giftLoc, ArmorStand.class, as -> {
            as.customName(Component.text("🎁 Подарок", NamedTextColor.GREEN));
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
            p.sendMessage(Component.text("🎅 Тёмный Санта бросает подарок!", NamedTextColor.RED));
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
                // Взрыв!
                giftLoc.getWorld().createExplosion(giftLoc, 3f, false, false);
                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1)); // 5 сек
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
        int count = 3 + new Random().nextInt(3); // 3-5

        for (Player p : world.getNearbyPlayers(loc, 30)) {
            p.sendMessage(Component.text("🎅 Тёмный Санта призывает Эльфов!", NamedTextColor.RED));
        }

        for (int i = 0; i < count; i++) {
            world.spawn(loc.clone().add(
                    (Math.random() - 0.5) * 4, 1, (Math.random() - 0.5) * 4
            ), Vex.class, vex -> {
                vex.customName(Component.text("Злой Эльф", NamedTextColor.DARK_GREEN));
                vex.setCustomNameVisible(true);

                // Зелёный шлем
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
            p.addPotionEffect(new PotionEffect(effect, 60, 1)); // 3 сек
            p.sendMessage(Component.text("🎅 Мешок Хаоса! Вы получили: " + formatEffectName(effect), NamedTextColor.DARK_PURPLE));
        }

        world.spawnParticle(Particle.WITCH, loc, 80, 10, 3, 10, 0.1);
        world.playSound(loc, Sound.ENTITY_EVOKER_CELEBRATE, 2f, 0.5f);
    }

    private String formatEffectName(PotionEffectType type) {
        if (type == PotionEffectType.BLINDNESS) return "Слепота";
        if (type == PotionEffectType.POISON) return "Отравление";
        if (type == PotionEffectType.LEVITATION) return "Левитация";
        if (type == PotionEffectType.WEAKNESS) return "Слабость";
        if (type == PotionEffectType.NAUSEA) return "Тошнота";
        return type.getKey().getKey();
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

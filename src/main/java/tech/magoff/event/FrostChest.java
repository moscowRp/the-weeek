package tech.magoff.event;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import tech.magoff.loot.LootTable;
import tech.magoff.theweeek;
import tech.magoff.util.TextUtil;

import java.util.List;

public class FrostChest {

    private final theweeek plugin;
    private Location chestLocation;
    private ArmorStand hologram;
    private BukkitRunnable timerTask;
    private int secondsLeft;
    private boolean unlocked = false;
    private boolean looted = false;

    public FrostChest(theweeek plugin) {
        this.plugin = plugin;
        this.secondsLeft = 120;
    }

    public void spawn(World world) {
        Location center = world.getSpawnLocation();
        int radius = 500;

        int x = center.getBlockX() + (int) ((Math.random() - 0.5) * 2 * radius);
        int z = center.getBlockZ() + (int) ((Math.random() - 0.5) * 2 * radius);
        int y = world.getHighestBlockYAt(x, z) + 1;

        chestLocation = new Location(world, x, y, z);

        Block block = chestLocation.getBlock();
        block.setType(Material.CHEST);

        Location holoLoc = chestLocation.clone().add(0.5, 2.0, 0.5);
        hologram = world.spawn(holoLoc, ArmorStand.class, as -> {
            as.customName(TextUtil.parse("<gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("Сундук Вечной Зимы") + "</b></gradient> <dark_gray>•</dark_gray> <#ffd200><b>02:00</b></#ffd200>"));
            as.setCustomNameVisible(true);
            as.setVisible(false);
            as.setGravity(false);
            as.setMarker(true);
            as.setSmall(true);
            as.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "frost_chest_hologram"),
                    PersistentDataType.BOOLEAN,
                    true
            );
        });

        for (Player p : world.getPlayers()) {
            p.sendMessage(Component.empty());
            p.sendMessage(TextUtil.parse("<dark_gray>╭────────────────────────────────────────╮</dark_gray>"));
            p.sendMessage(TextUtil.parse("   <gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("СУНДУК ВЕЧНОЙ ЗИМЫ ПОЯВИЛСЯ") + "</b></gradient>"));
            p.sendMessage(TextUtil.parse("   <gray>" + TextUtil.toSmallCaps("Локация:") + "</gray> <gradient:#ffd200:#f7971e><b>X: " + x + "  Y: " + y + "  Z: " + z + "</b></gradient>"));
            p.sendMessage(TextUtil.parse("   <gray>" + TextUtil.toSmallCaps("Статус:") + "</gray> <#ff4b2b>" + TextUtil.toSmallCaps("Заморожен на 120 секунд") + "</#ff4b2b>"));
            p.sendMessage(TextUtil.parse("   <gray>" + TextUtil.toSmallCaps("Пробивайтесь к сундуку и удерживайте позицию!") + "</gray>"));
            p.sendMessage(TextUtil.parse("<dark_gray>╰────────────────────────────────────────╯</dark_gray>"));
            p.sendMessage(Component.empty());

            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }

        startTimer();
        plugin.getLogger().info("Сундук Вечной Зимы заспавнен на " + x + ", " + y + ", " + z);
    }

    private void startTimer() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (looted) {
                    cancel();
                    return;
                }

                secondsLeft--;

                if (hologram != null && hologram.isValid()) {
                    String time = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60);
                    String timeColor = secondsLeft <= 15 ? "<#ff4b2b>" : "<#ffd200>";
                    hologram.customName(TextUtil.parse("<gradient:#00f2fe:#4facfe><b>❄ " + TextUtil.toSmallCaps("Сундук Вечной Зимы") + "</b></gradient> <dark_gray>•</dark_gray> " + timeColor + "<b>" + time + "</b>" + timeColor.replace("<", "</")));
                }

                if (chestLocation != null) {
                    Location particleLoc = chestLocation.clone().add(0.5, 1, 0.5);
                    chestLocation.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 5, 0.5, 1, 0.5, 0.02);
                    chestLocation.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 10, 2, 2, 2, 0.01);
                }

                if (secondsLeft > 0 && secondsLeft % 30 == 0) {
                    spawnGuards();
                }

                if (secondsLeft == 30 || secondsLeft == 10 || secondsLeft == 5) {
                    for (Player p : chestLocation.getWorld().getPlayers()) {
                        p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ffd200:#f7971e>" + TextUtil.toSmallCaps("Сундук откроется через " + secondsLeft + " сек!") + "</gradient>"));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.5f);
                    }
                }

                if (secondsLeft <= 0) {
                    unlock();
                    cancel();
                }
            }
        };
        timerTask.runTaskTimer(plugin, 20L, 20L);
    }

    private void spawnGuards() {
        if (chestLocation == null) return;
        World world = chestLocation.getWorld();

        for (Player p : world.getNearbyPlayers(chestLocation, 30)) {
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff416c:#ff4b2b>" + TextUtil.toSmallCaps("Ледяные стражи прибыли на защиту сундука!") + "</gradient>"));
        }

        for (int i = 0; i < 4; i++) {
            Location spawnLoc = chestLocation.clone().add(
                    (Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10
            );
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);

            world.spawn(spawnLoc, org.bukkit.entity.Stray.class, stray -> {
                stray.customName(TextUtil.parse("<#70a1ff>" + TextUtil.toSmallCaps("Страж Зимы") + "</#70a1ff>"));
                stray.setCustomNameVisible(true);
                stray.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                stray.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
            });
        }

        world.playSound(chestLocation, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
    }

    private void unlock() {
        unlocked = true;

        if (hologram != null && hologram.isValid()) {
            hologram.customName(TextUtil.parse("<gradient:#43e97b:#38f9d7><b>✨ " + TextUtil.toSmallCaps("СУНДУК РАЗБЛОКИРОВАН!") + "</b></gradient> <dark_gray>•</dark_gray> <gray>" + TextUtil.toSmallCaps("Забирай лут") + "</gray>"));
        }

        if (chestLocation != null) {
            Block block = chestLocation.getBlock();
            if (block.getState() instanceof Chest chest) {
                Inventory inv = chest.getInventory();
                List<ItemStack> loot = LootTable.generateChestLoot();
                for (int i = 0; i < loot.size() && i < inv.getSize(); i++) {
                    inv.setItem(i, loot.get(i));
                }
                chest.update();
            }
        }

        for (Player p : chestLocation.getWorld().getPlayers()) {
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#43e97b:#38f9d7><b>" + TextUtil.toSmallCaps("Сундук Вечной Зимы РАЗМОРОЖЕН!") + "</b></gradient> <gray>" + TextUtil.toSmallCaps("Первый забирает всё!") + "</gray>"));
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        }

        chestLocation.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, chestLocation.clone().add(0.5, 1, 0.5), 100, 1, 2, 1, 0.3);
    }

    public boolean isChestLocation(Location location) {
        if (chestLocation == null) return false;
        return chestLocation.getBlockX() == location.getBlockX()
                && chestLocation.getBlockY() == location.getBlockY()
                && chestLocation.getBlockZ() == location.getBlockZ()
                && chestLocation.getWorld().equals(location.getWorld());
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public boolean isLooted() {
        return looted;
    }

    public void setLooted(Player player) {
        this.looted = true;

        for (Player p : chestLocation.getWorld().getPlayers()) {
            p.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ffd200:#f7971e><b>👑 " + player.getName() + "</b></gradient> <gray>" + TextUtil.toSmallCaps("забрал главные сокровища из Сундука!") + "</gray>"));
        }

        player.giveExp(1000 + (int)(Math.random() * 2000));

        if (hologram != null && hologram.isValid()) {
            hologram.remove();
        }
    }

    public void cleanup() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
        }
        if (hologram != null && hologram.isValid()) {
            hologram.remove();
        }
        if (chestLocation != null) {
            chestLocation.getBlock().setType(Material.AIR);
        }
        looted = true;
    }
}

package tech.magoff.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

    /**
     * Спавнит сундук в случайной точке мира.
     */
    public void spawn(World world) {
        Location center = world.getSpawnLocation();
        int radius = 500;

        // Случайная точка в радиусе
        int x = center.getBlockX() + (int) ((Math.random() - 0.5) * 2 * radius);
        int z = center.getBlockZ() + (int) ((Math.random() - 0.5) * 2 * radius);
        int y = world.getHighestBlockYAt(x, z) + 1;

        chestLocation = new Location(world, x, y, z);

        // Ставим сундук
        Block block = chestLocation.getBlock();
        block.setType(Material.CHEST);

        // Создаём холограмму
        Location holoLoc = chestLocation.clone().add(0.5, 2.0, 0.5);
        hologram = world.spawn(holoLoc, ArmorStand.class, as -> {
            as.customName(Component.text("❄ Сундук Вечной Зимы — 2:00", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
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

        // Объявить координаты всем
        for (Player p : world.getPlayers()) {
            p.sendMessage(Component.empty());
            p.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.AQUA));
            p.sendMessage(Component.text("❄ СУНДУК ВЕЧНОЙ ЗИМЫ", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
            p.sendMessage(Component.text("Координаты: " + x + ", " + y + ", " + z, NamedTextColor.GOLD));
            p.sendMessage(Component.text("Откроется через 2 минуты!", NamedTextColor.GRAY));
            p.sendMessage(Component.text("Защищайте его и пробивайтесь!", NamedTextColor.YELLOW));
            p.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.AQUA));
            p.sendMessage(Component.empty());

            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }

        // Запуск таймера
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

                // Обновление холограммы
                if (hologram != null && hologram.isValid()) {
                    String time = String.format("%d:%02d", secondsLeft / 60, secondsLeft % 60);
                    NamedTextColor color = secondsLeft <= 10 ? NamedTextColor.RED : NamedTextColor.AQUA;
                    hologram.customName(Component.text("❄ Сундук Вечной Зимы — " + time, color).decorate(TextDecoration.BOLD));
                }

                // Частицы вокруг сундука
                if (chestLocation != null) {
                    Location particleLoc = chestLocation.clone().add(0.5, 1, 0.5);
                    chestLocation.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 5, 0.5, 1, 0.5, 0.02);
                    chestLocation.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 10, 2, 2, 2, 0.01);
                }

                // Спавн мобов-защитников каждые 30 сек
                if (secondsLeft > 0 && secondsLeft % 30 == 0) {
                    spawnGuards();
                }

                // Предупреждения
                if (secondsLeft == 30 || secondsLeft == 10 || secondsLeft == 5) {
                    for (Player p : chestLocation.getWorld().getPlayers()) {
                        p.sendMessage(Component.text("❄ Сундук откроется через " + secondsLeft + " сек!", NamedTextColor.YELLOW));
                    }
                }

                // Открытие!
                if (secondsLeft <= 0) {
                    unlock();
                    cancel();
                }
            }
        };
        timerTask.runTaskTimer(plugin, 20L, 20L); // каждую секунду
    }

    private void spawnGuards() {
        if (chestLocation == null) return;
        World world = chestLocation.getWorld();

        for (Player p : world.getNearbyPlayers(chestLocation, 30)) {
            p.sendMessage(Component.text("❄ Стражи зимы защищают сундук!", NamedTextColor.RED));
        }

        // Спавн Stray'ев вокруг сундука
        for (int i = 0; i < 4; i++) {
            Location spawnLoc = chestLocation.clone().add(
                    (Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10
            );
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);

            world.spawn(spawnLoc, org.bukkit.entity.Stray.class, stray -> {
                stray.customName(Component.text("Страж Зимы", NamedTextColor.WHITE));
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
            hologram.customName(Component.text("❄ СУНДУК ОТКРЫТ! Забирай лут!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        }

        // Наполнить сундук лутом
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

        // Объявить всем
        for (Player p : chestLocation.getWorld().getPlayers()) {
            p.sendMessage(Component.text("❄ Сундук Вечной Зимы РАЗБЛОКИРОВАН! Первый забирает всё!", NamedTextColor.GREEN));
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        }

        // Эффекты
        chestLocation.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, chestLocation.clone().add(0.5, 1, 0.5), 100, 1, 2, 1, 0.3);
    }

    /**
     * Проверяет, можно ли открыть сундук. Вызывается из листенера.
     */
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

        // Объявить победителя
        for (Player p : chestLocation.getWorld().getPlayers()) {
            p.sendMessage(Component.text("🏆 " + player.getName() + " забрал лут из Сундука Вечной Зимы!", NamedTextColor.GOLD));
        }

        // Опыт
        player.giveExp(1000 + (int)(Math.random() * 2000));

        // Убрать холограмму
        if (hologram != null && hologram.isValid()) {
            hologram.remove();
        }
    }

    /**
     * Очистка при остановке ивента.
     */
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

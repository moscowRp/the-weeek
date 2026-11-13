package tech.magoff.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tech.magoff.boss.BossManager;
import tech.magoff.event.EventManager;
import tech.magoff.event.FrostChest;
import tech.magoff.event.FrostSiegeEvent;
import tech.magoff.loot.FrostItems;
import tech.magoff.theweeek;

public class EventListener implements Listener {

    private final theweeek plugin;
    private final BossManager bossManager;
    private final EventManager eventManager;

    public EventListener(theweeek plugin, BossManager bossManager, EventManager eventManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        this.eventManager = eventManager;
    }

    // ============ Смерть босса ============
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!bossManager.isBoss(entity.getUniqueId())) return;

        // Отменяем стандартный дроп
        event.getDrops().clear();
        event.setDroppedExp(0);

        // Обработка через BossManager
        bossManager.onBossDeath(entity.getUniqueId(), entity.getLocation());
    }

    // ============ Открытие сундука ивента ============
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        if (!eventManager.isEventActive()) return;

        FrostSiegeEvent siege = eventManager.getCurrentEvent();
        if (siege == null) return;

        FrostChest frostChest = siege.getFrostChest();
        if (frostChest == null) return;

        if (!frostChest.isChestLocation(event.getClickedBlock().getLocation())) return;

        Player player = event.getPlayer();

        if (frostChest.isLooted()) {
            player.sendMessage(Component.text("❄ Сундук уже был разграблен!", NamedTextColor.GRAY));
            event.setCancelled(true);
            return;
        }

        if (!frostChest.isUnlocked()) {
            player.sendMessage(Component.text("❄ Сундук ещё заморожен! Дождитесь окончания таймера.", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // Сундук открыт! Помечаем как разграбленный после того, как игрок его откроет
        // Позволяем открыть (не отменяем event), но помечаем
        frostChest.setLooted(player);
    }

    // ============ Кулак Мороза — замедление при ударе ============
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        var weapon = player.getInventory().getItemInMainHand();
        if (FrostItems.isFrostItem(weapon, "fist_of_frost")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2)); // 3 сек, Slowness III
        }

        if (FrostItems.isFrostItem(weapon, "blizzard_bow")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1)); // 4 сек, Slowness II
        }
    }

    // ============ Корона Метели — аура замедления ============
    // Вызывается через отдельный таймер в главном классе
    public static void tickBlizzardCrown(Player player) {
        var helmet = player.getInventory().getHelmet();
        if (FrostItems.isFrostItem(helmet, "blizzard_crown")) {
            for (Entity nearby : player.getNearbyEntities(8, 8, 8)) {
                if (nearby instanceof LivingEntity le && !(nearby instanceof Player)) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                }
            }
        }
    }
}

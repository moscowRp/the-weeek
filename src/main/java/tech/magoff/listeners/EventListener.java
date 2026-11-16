package tech.magoff.listeners;

import net.kyori.adventure.text.Component;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tech.magoff.boss.BossManager;
import tech.magoff.event.EventManager;
import tech.magoff.event.FrostChest;
import tech.magoff.event.FrostSiegeEvent;
import tech.magoff.loot.FrostItems;
import tech.magoff.theweeek;
import tech.magoff.util.TextUtil;

public class EventListener implements Listener {

    private final theweeek plugin;
    private final BossManager bossManager;
    private final EventManager eventManager;

    public EventListener(theweeek plugin, BossManager bossManager, EventManager eventManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        this.eventManager = eventManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!bossManager.isBoss(entity.getUniqueId())) return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        bossManager.onBossDeath(entity.getUniqueId(), entity.getLocation());
    }

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
            player.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gray>" + TextUtil.toSmallCaps("Сундук уже был разграблен другими искателями приключений!") + "</gray>"));
            event.setCancelled(true);
            return;
        }

        if (!frostChest.isUnlocked()) {
            player.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff416c:#ff4b2b>" + TextUtil.toSmallCaps("Сундук скован древним льдом! Дождитесь разморозки.") + "</gradient>"));
            event.setCancelled(true);
            return;
        }

        frostChest.setLooted(player);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        var weapon = player.getInventory().getItemInMainHand();
        if (FrostItems.isFrostItem(weapon, "fist_of_frost")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
        }

        if (FrostItems.isFrostItem(weapon, "blizzard_bow")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
        }
    }

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

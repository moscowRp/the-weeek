package tech.magoff.event;

import org.bukkit.scheduler.BukkitRunnable;
import tech.magoff.boss.BossManager;
import tech.magoff.theweeek;

public class EventManager {

    private final theweeek plugin;
    private final BossManager bossManager;
    private FrostSiegeEvent currentEvent;
    private BukkitRunnable autoStartTask;

    public EventManager(theweeek plugin, BossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
    }

    /**
     * Запускает автоматический таймер ивентов.
     */
    public void startAutoTimer() {
        int interval = plugin.getConfig().getInt("events.interval", 300); // секунды

        autoStartTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEventActive()) {
                    startEvent();
                }
            }
        };
        autoStartTask.runTaskTimer(plugin, interval * 20L, interval * 20L);

        plugin.getLogger().info("Автотаймер ивентов запущен (интервал: " + interval + " сек)");
    }

    /**
     * Останавливает автоматический таймер.
     */
    public void stopAutoTimer() {
        if (autoStartTask != null && !autoStartTask.isCancelled()) {
            autoStartTask.cancel();
        }
    }

    /**
     * Запуск ивента вручную или по таймеру.
     */
    public void startEvent() {
        if (currentEvent != null && currentEvent.isActive()) {
            return; // Ивент уже идёт
        }

        currentEvent = new FrostSiegeEvent(plugin, bossManager);
        currentEvent.start();

        plugin.getLogger().info("Ивент «Вечная Зима» запущен!");
    }

    /**
     * Принудительная остановка ивента.
     */
    public void stopEvent() {
        if (currentEvent != null && currentEvent.isActive()) {
            currentEvent.forceStop();
            plugin.getLogger().info("Ивент «Вечная Зима» остановлен принудительно.");
        }
    }

    public boolean isEventActive() {
        return currentEvent != null && currentEvent.isActive();
    }

    public FrostSiegeEvent getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Полная очистка при выключении плагина.
     */
    public void shutdown() {
        stopAutoTimer();
        if (currentEvent != null && currentEvent.isActive()) {
            currentEvent.forceStop();
        }
    }
}

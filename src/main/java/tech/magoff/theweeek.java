package tech.magoff;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import tech.magoff.boss.BossManager;
import tech.magoff.commands.WCCommand;
import tech.magoff.event.EventManager;
import tech.magoff.listeners.EventListener;

import java.util.Objects;

public final class theweeek extends JavaPlugin {

    private static theweeek instance;
    private BossManager bossManager;
    private EventManager eventManager;
    private BukkitRunnable passiveEffectsTask;

    @Override
    public void onEnable() {
        instance = this;

        // Загрузка конфигурации
        saveDefaultConfig();

        // Инициализация менеджеров
        bossManager = new BossManager(this);
        eventManager = new EventManager(this, bossManager);

        // Регистрация команд
        WCCommand commandExecutor = new WCCommand(this, eventManager, bossManager);
        Objects.requireNonNull(getCommand("wc")).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand("wc")).setTabCompleter(commandExecutor);

        // Регистрация слушателей событий
        getServer().getPluginManager().registerEvents(new EventListener(this, bossManager, eventManager), this);

        // Запуск пассивных эффектов (Корона Метели и др.) раз в секунду
        passiveEffectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    EventListener.tickBlizzardCrown(player);
                }
            }
        };
        passiveEffectsTask.runTaskTimer(this, 20L, 20L);

        // Запуск автотаймера ивентов
        eventManager.startAutoTimer();

        getLogger().info("WINTER CLASH VER 1488 CODER SMAZZI IZIZIZIZIZIZIIZ JKLJKLKFJ I PIDOR HAHAHA YOU CRACKED 101010101010101010101010101010");
    }

    @Override
    public void onDisable() {
        if (passiveEffectsTask != null && !passiveEffectsTask.isCancelled()) {
            passiveEffectsTask.cancel();
        }

        if (eventManager != null) {
            eventManager.shutdown();
        }

        if (bossManager != null) {
            bossManager.cleanupAll();
        }

        getLogger().info("WinterClash упал((((((");
    }

    public static theweeek getInstance() {
        return instance;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}

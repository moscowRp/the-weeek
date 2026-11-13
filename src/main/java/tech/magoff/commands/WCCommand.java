package tech.magoff.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import tech.magoff.event.EventManager;
import tech.magoff.theweeek;

import java.util.ArrayList;
import java.util.List;

public class WCCommand implements CommandExecutor, TabCompleter {

    private final theweeek plugin;
    private final EventManager eventManager;

    public WCCommand(theweeek plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("winterclash.admin")) {
            sender.sendMessage(Component.text("❌ У вас нет прав!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (eventManager.isEventActive()) {
                    sender.sendMessage(Component.text("❄ Ивент уже идёт!", NamedTextColor.YELLOW));
                } else {
                    eventManager.startEvent();
                    sender.sendMessage(Component.text("❄ Ивент «Вечная Зима» запущен!", NamedTextColor.GREEN));
                }
            }
            case "stop" -> {
                if (!eventManager.isEventActive()) {
                    sender.sendMessage(Component.text("❄ Ивент не запущен.", NamedTextColor.YELLOW));
                } else {
                    eventManager.stopEvent();
                    sender.sendMessage(Component.text("❄ Ивент остановлен.", NamedTextColor.RED));
                }
            }
            case "status" -> {
                if (eventManager.isEventActive()) {
                    sender.sendMessage(Component.text("❄ Ивент активен.", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("❄ Ивент не активен.", NamedTextColor.GRAY));
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(Component.text("❄ Конфиг перезагружен.", NamedTextColor.GREEN));
            }
            case "autotimer" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Использование: /wc autotimer <on|off>", NamedTextColor.YELLOW));
                    return true;
                }
                if (args[1].equalsIgnoreCase("on")) {
                    eventManager.startAutoTimer();
                    sender.sendMessage(Component.text("❄ Автотаймер включён.", NamedTextColor.GREEN));
                } else if (args[1].equalsIgnoreCase("off")) {
                    eventManager.stopAutoTimer();
                    sender.sendMessage(Component.text("❄ Автотаймер выключен.", NamedTextColor.RED));
                }
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("═══ WinterClash ═══", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text("/wc start", NamedTextColor.GOLD)
                .append(Component.text(" — Запустить ивент", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/wc stop", NamedTextColor.GOLD)
                .append(Component.text(" — Остановить ивент", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/wc status", NamedTextColor.GOLD)
                .append(Component.text(" — Статус ивента", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/wc autotimer <on|off>", NamedTextColor.GOLD)
                .append(Component.text(" — Автозапуск ивентов", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/wc reload", NamedTextColor.GOLD)
                .append(Component.text(" — Перезагрузить конфиг", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("winterclash.admin")) return completions;

        if (args.length == 1) {
            List<String> subs = List.of("start", "stop", "status", "autotimer", "reload");
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("autotimer")) {
            if ("on".startsWith(args[1].toLowerCase())) completions.add("on");
            if ("off".startsWith(args[1].toLowerCase())) completions.add("off");
        }

        return completions;
    }
}

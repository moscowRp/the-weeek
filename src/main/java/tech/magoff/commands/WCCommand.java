package tech.magoff.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import tech.magoff.boss.BossManager;
import tech.magoff.event.EventManager;
import tech.magoff.loot.FrostItems;
import tech.magoff.theweeek;

import java.util.ArrayList;
import java.util.List;

public class WCCommand implements CommandExecutor, TabCompleter {

    private final theweeek plugin;
    private final EventManager eventManager;
    private final BossManager bossManager;

    public WCCommand(theweeek plugin, EventManager eventManager, BossManager bossManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
        this.bossManager = bossManager;
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
                    sender.sendMessage(Component.text("❄ Ивент активен. Живых боссов: " + bossManager.getActiveBossCount(), NamedTextColor.GREEN));
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
            case "spawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("❌ Только для игроков!", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Использование: /wc spawn <golem|santa|queen>", NamedTextColor.YELLOW));
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "golem" -> {
                        bossManager.spawnFrostGolem(player.getLocation());
                        player.sendMessage(Component.text("❄ Ледяной Голем заспавнен!", NamedTextColor.AQUA));
                    }
                    case "santa" -> {
                        bossManager.spawnDarkSanta(player.getLocation());
                        player.sendMessage(Component.text("🎅 Тёмный Санта заспавнен!", NamedTextColor.DARK_RED));
                    }
                    case "queen" -> {
                        bossManager.spawnBlizzardQueen(player.getLocation());
                        player.sendMessage(Component.text("🌨 Королева Метели заспавнена!", NamedTextColor.BLUE));
                    }
                    default -> player.sendMessage(Component.text("Неизвестный босс. Доступны: golem, santa, queen", NamedTextColor.RED));
                }
            }
            case "give" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Использование: /wc give <shard|heart|crown|fist|bow|bag> [игрок]", NamedTextColor.YELLOW));
                    return true;
                }
                Player targetPlayer;
                if (args.length >= 3) {
                    targetPlayer = Bukkit.getPlayer(args[2]);
                    if (targetPlayer == null) {
                        sender.sendMessage(Component.text("Игрок не найден!", NamedTextColor.RED));
                        return true;
                    }
                } else if (sender instanceof Player p) {
                    targetPlayer = p;
                } else {
                    sender.sendMessage(Component.text("Укажите имя игрока!", NamedTextColor.RED));
                    return true;
                }

                ItemStack item = switch (args[1].toLowerCase()) {
                    case "shard" -> FrostItems.frostShard();
                    case "heart" -> FrostItems.heartOfWinter();
                    case "crown" -> FrostItems.blizzardCrown();
                    case "fist" -> FrostItems.fistOfFrost();
                    case "bow" -> FrostItems.blizzardBow();
                    case "bag" -> FrostItems.santaBag();
                    default -> null;
                };

                if (item == null) {
                    sender.sendMessage(Component.text("Неизвестный предмет! Доступны: shard, heart, crown, fist, bow, bag", NamedTextColor.RED));
                    return true;
                }

                targetPlayer.getInventory().addItem(item);
                sender.sendMessage(Component.text("Предмет выдан игроку " + targetPlayer.getName(), NamedTextColor.GREEN));
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
                .append(Component.text(" — Статус ивента и боссов", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/wc spawn <golem|santa|queen>", NamedTextColor.GOLD)
                .append(Component.text(" — Заспавнить босса", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/wc give <item> [player]", NamedTextColor.GOLD)
                .append(Component.text(" — Выдать кастомный предмет", NamedTextColor.GRAY)));
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
            List<String> subs = List.of("start", "stop", "status", "spawn", "give", "autotimer", "reload");
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            List<String> bosses = List.of("golem", "santa", "queen");
            for (String b : bosses) {
                if (b.startsWith(args[1].toLowerCase())) completions.add(b);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> items = List.of("shard", "heart", "crown", "fist", "bow", "bag");
            for (String it : items) {
                if (it.startsWith(args[1].toLowerCase())) completions.add(it);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("autotimer")) {
            if ("on".startsWith(args[1].toLowerCase())) completions.add("on");
            if ("off".startsWith(args[1].toLowerCase())) completions.add("off");
        }

        return completions;
    }
}

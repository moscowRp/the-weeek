package tech.magoff.commands;

import net.kyori.adventure.text.Component;
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
import tech.magoff.util.TextUtil;

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
            sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff416c:#ff4b2b>❌ " + TextUtil.toSmallCaps("У вас нет прав на эту команду!") + "</gradient>"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (eventManager.isEventActive()) {
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ffd200:#f7971e>" + TextUtil.toSmallCaps("Ивент уже запущен и активен!") + "</gradient>"));
                } else {
                    eventManager.startEvent();
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#43e97b:#38f9d7>" + TextUtil.toSmallCaps("Ивент «Вечная Зима» успешно запущен!") + "</gradient>"));
                }
            }
            case "stop" -> {
                if (!eventManager.isEventActive()) {
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gray>" + TextUtil.toSmallCaps("В данный момент нет активных ивентов.") + "</gray>"));
                } else {
                    eventManager.stopEvent();
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff416c:#ff4b2b>" + TextUtil.toSmallCaps("Ивент был принудительно остановлен.") + "</gradient>"));
                }
            }
            case "status" -> {
                if (eventManager.isEventActive()) {
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#43e97b:#38f9d7>" + TextUtil.toSmallCaps("Ивент АКТИВЕН.") + "</gradient> <gray>" + TextUtil.toSmallCaps("Боссов в живых:") + "</gray> <#ffd200><b>" + bossManager.getActiveBossCount() + "</b></#ffd200>"));
                } else {
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gray>" + TextUtil.toSmallCaps("Ивент НЕ АКТИВЕН.") + "</gray>"));
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#43e97b:#38f9d7>" + TextUtil.toSmallCaps("Конфигурация успешно перезагружена!") + "</gradient>"));
            }
            case "autotimer" -> {
                if (args.length < 2) {
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gray>" + TextUtil.toSmallCaps("Использование:") + "</gray> <#ffd200>/wc autotimer <on|off></#ffd200>"));
                    return true;
                }
                if (args[1].equalsIgnoreCase("on")) {
                    eventManager.startAutoTimer();
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#43e97b:#38f9d7>" + TextUtil.toSmallCaps("Автотаймер ивентов включен.") + "</gradient>"));
                } else if (args[1].equalsIgnoreCase("off")) {
                    eventManager.stopAutoTimer();
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff416c:#ff4b2b>" + TextUtil.toSmallCaps("Автотаймер ивентов выключен.") + "</gradient>"));
                }
            }
            case "spawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<#ff4b2b>" + TextUtil.toSmallCaps("Команда доступна только игрокам в игре!") + "</#ff4b2b>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gray>" + TextUtil.toSmallCaps("Использование:") + "</gray> <#ffd200>/wc spawn <golem|santa|queen></#ffd200>"));
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "golem" -> {
                        bossManager.spawnFrostGolem(player.getLocation());
                        player.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#00f2fe:#4facfe>" + TextUtil.toSmallCaps("Ледяной Голем заспавнен рядом с вами!") + "</gradient>"));
                    }
                    case "santa" -> {
                        bossManager.spawnDarkSanta(player.getLocation());
                        player.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#ff0844:#ffb199>" + TextUtil.toSmallCaps("Тёмный Санта заспавнен рядом с вами!") + "</gradient>"));
                    }
                    case "queen" -> {
                        bossManager.spawnBlizzardQueen(player.getLocation());
                        player.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#b176ff:#e0c3fc>" + TextUtil.toSmallCaps("Королева Метели заспавнена рядом с вами!") + "</gradient>"));
                    }
                    default -> player.sendMessage(TextUtil.parse(TextUtil.prefix() + "<#ff4b2b>" + TextUtil.toSmallCaps("Неизвестный босс. Доступны: golem, santa, queen") + "</#ff4b2b>"));
                }
            }
            case "give" -> {
                if (args.length < 2) {
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gray>" + TextUtil.toSmallCaps("Использование:") + "</gray> <#ffd200>/wc give <shard|heart|crown|fist|bow|bag> [игрок]</#ffd200>"));
                    return true;
                }
                Player targetPlayer;
                if (args.length >= 3) {
                    targetPlayer = Bukkit.getPlayer(args[2]);
                    if (targetPlayer == null) {
                        sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<#ff4b2b>" + TextUtil.toSmallCaps("Игрок не найден в сети!") + "</#ff4b2b>"));
                        return true;
                    }
                } else if (sender instanceof Player p) {
                    targetPlayer = p;
                } else {
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<#ff4b2b>" + TextUtil.toSmallCaps("Укажите имя игрока при вызове из консоли!") + "</#ff4b2b>"));
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
                    sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<#ff4b2b>" + TextUtil.toSmallCaps("Неизвестный предмет! Доступны: shard, heart, crown, fist, bow, bag") + "</#ff4b2b>"));
                    return true;
                }

                targetPlayer.getInventory().addItem(item);
                sender.sendMessage(TextUtil.parse(TextUtil.prefix() + "<gradient:#43e97b:#38f9d7>" + TextUtil.toSmallCaps("Предмет успешно выдан игроку") + " <b>" + targetPlayer.getName() + "</b></gradient>"));
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(TextUtil.parse("<dark_gray>╭────────────────────────────────────────╮</dark_gray>"));
        sender.sendMessage(TextUtil.parse("   <gradient:#00f2fe:#4facfe><b>❄ ᴡɪɴᴛᴇʀᴄʟᴀꜱʜ • ᴀᴅᴍɪɴ ᴘᴀɴᴇʟ</b></gradient>"));
        sender.sendMessage(TextUtil.parse("   <#00f2fe>/wc start</#00f2fe> <dark_gray>—</dark_gray> <gray>" + TextUtil.toSmallCaps("Запустить глобальный ивент") + "</gray>"));
        sender.sendMessage(TextUtil.parse("   <#00f2fe>/wc stop</#00f2fe> <dark_gray>—</dark_gray> <gray>" + TextUtil.toSmallCaps("Остановить активный ивент") + "</gray>"));
        sender.sendMessage(TextUtil.parse("   <#00f2fe>/wc status</#00f2fe> <dark_gray>—</dark_gray> <gray>" + TextUtil.toSmallCaps("Статус ивента и живых боссов") + "</gray>"));
        sender.sendMessage(TextUtil.parse("   <#00f2fe>/wc spawn <golem|santa|queen></#00f2fe> <dark_gray>—</dark_gray> <gray>" + TextUtil.toSmallCaps("Призвать босса") + "</gray>"));
        sender.sendMessage(TextUtil.parse("   <#00f2fe>/wc give <item> [ник]</#00f2fe> <dark_gray>—</dark_gray> <gray>" + TextUtil.toSmallCaps("Выдать кастомный предмет") + "</gray>"));
        sender.sendMessage(TextUtil.parse("   <#00f2fe>/wc autotimer <on|off></#00f2fe> <dark_gray>—</dark_gray> <gray>" + TextUtil.toSmallCaps("Управление авто-таймером") + "</gray>"));
        sender.sendMessage(TextUtil.parse("   <#00f2fe>/wc reload</#00f2fe> <dark_gray>—</dark_gray> <gray>" + TextUtil.toSmallCaps("Перезагрузить config.yml") + "</gray>"));
        sender.sendMessage(TextUtil.parse("<dark_gray>╰────────────────────────────────────────╯</dark_gray>"));
        sender.sendMessage(Component.empty());
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

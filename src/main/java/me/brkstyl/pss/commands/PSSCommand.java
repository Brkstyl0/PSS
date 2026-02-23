package me.brkstyl.pss.commands;

import me.brkstyl.pss.PluginMain;
import me.brkstyl.pss.utils.ColorUtils;
import net.kyori.adventure.text.Component; // Import eklendi
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PSSCommand implements CommandExecutor, TabCompleter {

    private final PluginMain plugin;

    public PSSCommand(PluginMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        boolean hasAnyCorePerm = sender.hasPermission("pss.admin")
                || sender.hasPermission("pss.maintenance")
                || sender.hasPermission("pss.moderation");
        if (!hasAnyCorePerm) {
            sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
            return true;
        }

        if (args.length > 0) {
            String sub = args[0].toLowerCase();

            if (sub.equals("reload")) {
                if (!sender.hasPermission("pss.admin")) {
                    sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getLanguageManager().loadLanguage();

                // DÜZELTME: Prefix ve Mesajı ayrı formatlayıp birleştiriyoruz
                Component prefix = ColorUtils.format(plugin.getLanguageManager().getMessage("prefix"));
                Component msg = ColorUtils.format(plugin.getLanguageManager().getMessage("reload-success"));
                sender.sendMessage(prefix.append(msg));
                return true;
            }

            if (sub.equals("menu")) {
                if (sender instanceof Player) {
                    plugin.getMenuHandler().openMainMenu((Player) sender);
                } else {
                    sender.sendMessage("This command can only be used by players!");
                }
                return true;
            }

            if (sub.equals("maintenance")) {
                if (!(sender.hasPermission("pss.admin") || sender.hasPermission("pss.maintenance"))) {
                    sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
                    return true;
                }
                toggleMaintenance(sender);
                return true;
            }

            if (sub.equals("addbotname") && args.length > 1) {
                if (!sender.hasPermission("pss.admin")) {
                    sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
                    return true;
                }
                List<String> list = plugin.getConfig().getStringList("bot-protection.blacklisted-names");
                if (!list.contains(args[1])) {
                    list.add(args[1]);
                    plugin.getConfig().set("bot-protection.blacklisted-names", list);
                    plugin.saveConfig();

                    Component prefix = ColorUtils.format(plugin.getLanguageManager().getMessage("prefix"));
                    Component msg = ColorUtils.format("<green>Added '" + args[1] + "' to blacklist.");
                    sender.sendMessage(prefix.append(msg));
                }
                return true;
            }

            if (sub.equals("removebotname") && args.length > 1) {
                if (!sender.hasPermission("pss.admin")) {
                    sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
                    return true;
                }
                List<String> list = plugin.getConfig().getStringList("bot-protection.blacklisted-names");
                if (list.remove(args[1])) {
                    plugin.getConfig().set("bot-protection.blacklisted-names", list);
                    plugin.saveConfig();

                    Component prefix = ColorUtils.format(plugin.getLanguageManager().getMessage("prefix"));
                    Component msg = ColorUtils.format("<red>Removed '" + args[1] + "' from blacklist.");
                    sender.sendMessage(prefix.append(msg));
                }
                return true;
            }
        }

        sendHelpMessage(sender);
        return true;
    }

    private void toggleMaintenance(CommandSender sender) {
        boolean newState = !plugin.getConfig().getBoolean("maintenance.enabled", false);
        plugin.getConfig().set("maintenance.enabled", newState);
        plugin.saveConfig();

        String msgKey = newState ? "maintenance-enabled" : "maintenance-disabled";

        // DÜZELTME: Component birleştirme
        Component prefix = ColorUtils.format(plugin.getLanguageManager().getMessage("prefix"));
        Component msg = ColorUtils.format(plugin.getLanguageManager().getMessage(msgKey));
        sender.sendMessage(prefix.append(msg));
    }

    private void sendHelpMessage(CommandSender sender) {
        // pluginMeta().getVersion() kullanıyoruz çünkü build.gradle'dan otomatik çekiyor
        String version = plugin.getPluginMeta().getVersion();

        sender.sendMessage(ColorUtils.format("<blue>PSS</blue> <gray>v" + version + " - Developed by Burak"));
        sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("help-usage-title")));
        sender.sendMessage(ColorUtils.format(" <white>/pss menu</white> <gray>- Open admin panel</gray>"));

        String reportLine = plugin.getLanguageManager().getMessage("help-report");
        if (reportLine.startsWith("Message path not found")) {
            String lang = plugin.getConfig().getString("language", "en");
            if (lang.equalsIgnoreCase("tr")) {
                reportLine = " <white>/report <oyuncu></white> <gray>- Şüpheli bir oyuncuyu report et</gray>";
            } else {
                reportLine = " <white>/report <player></white> <gray>- Report a suspicious player</gray>";
            }
        }
        sender.sendMessage(ColorUtils.format(reportLine));
        sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("help-reload")));
        sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("help-maintenance")));
        sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("help-addbotname")));
        sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("help-removebotname")));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("menu");
            completions.add("maintenance");
            completions.add("addbotname");
            completions.add("removebotname");
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("removebotname")) {
            completions.addAll(plugin.getConfig().getStringList("bot-protection.blacklisted-names"));
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
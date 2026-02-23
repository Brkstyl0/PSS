package me.brkstyl.pss.commands;

import me.brkstyl.pss.PluginMain;
import me.brkstyl.pss.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportCommand implements CommandExecutor, TabCompleter {
    private final PluginMain plugin;

    public ReportCommand(PluginMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player reporter)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (!sender.hasPermission("pss.report")) {
            sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("report-usage")));
            return true;
        }

        String targetName = args[0];
        if (reporter.getName().equalsIgnoreCase(targetName)) {
            sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("report-self")));
            return true;
        }

        OfflinePlayer reported = Bukkit.getPlayerExact(targetName);
        if (reported == null) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
            if (offline.hasPlayedBefore()) {
                reported = offline;
            }
        }

        if (reported == null) {
            sender.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("report-player-not-found")));
            return true;
        }

        try {
            plugin.getReportService().report(reporter, reported, System.currentTimeMillis());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save report: " + e.getMessage());
            sender.sendMessage(ColorUtils.format("<red>Internal error.</red>"));
            return true;
        }

        Component prefix = ColorUtils.format(plugin.getLanguageManager().getMessage("prefix"));
        String template = plugin.getLanguageManager().getMessage("report-success");
        Component msg = ColorUtils.format(template.replace("<player>", reported.getName() != null ? reported.getName() : targetName));
        reporter.sendMessage(prefix.append(msg));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        }

        String last = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(last))
                .collect(Collectors.toList());
    }
}

package me.brkstyl.pss.listeners;

import me.brkstyl.pss.PluginMain;
import me.brkstyl.pss.utils.ColorUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import java.util.List;

public class ConnectionListener implements Listener {

    private final PluginMain plugin;

    public ConnectionListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();

        // 1. Bot Name Check
        List<String> blacklistedNames = plugin.getConfig().getStringList("bot-protection.blacklisted-names");
        for (String blacklisted : blacklistedNames) {
            if (playerName.contains(blacklisted)) {
                String kickMsg = plugin.getConfig().getString("bot-protection.kick-message", "<red>Blocked name.");
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ColorUtils.format(kickMsg));
                return;
            }
        }

        // 2. Maintenance Check
        if (plugin.getConfig().getBoolean("maintenance.enabled", false)) {
            if (!event.getPlayer().hasPermission("pss.maintenance.bypass") && !event.getPlayer().isOp()) {
                String kickMsg = plugin.getConfig().getString("maintenance.kick-message", "<red>Maintenance Mode");
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ColorUtils.format(kickMsg));
            }
        }
    }
}
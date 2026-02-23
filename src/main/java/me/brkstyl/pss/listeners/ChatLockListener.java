package me.brkstyl.pss.listeners;

import me.brkstyl.pss.PluginMain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatLockListener implements Listener {

    private final PluginMain plugin;

    public ChatLockListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.getSpectateManager() != null && plugin.getSpectateManager().isSpectating(player)) {
            return;
        }

        boolean chatEnabled = plugin.getConfig().getBoolean("chat.enabled", true);
        if (chatEnabled) return;

        if (player.isOp() || player.hasPermission("pss.bypasschat")) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(me.brkstyl.pss.utils.ColorUtils.format(plugin.getLanguageManager().getMessage("chat-disabled-message")));
    }
}

package me.brkstyl.pss.listeners;

import me.brkstyl.pss.PluginMain;
import me.brkstyl.pss.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FreezeListener implements Listener {

    private final PluginMain plugin;
    private final Map<UUID, Long> lastNotifyMillis = new HashMap<>();

    public FreezeListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    private boolean isFrozen(Player player) {
        boolean freezeEnabled = plugin.getConfig().getBoolean("freeze.enabled", false);
        if (!freezeEnabled) return false;

        if (plugin.getSpectateManager() != null && plugin.getSpectateManager().isSpectating(player)) {
            return false;
        }

        return !(player.isOp() || player.hasPermission("pss.bypassfreeze"));
    }

    private void notifyFrozen(Player player) {
        long now = System.currentTimeMillis();
        long last = lastNotifyMillis.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 1500) return;

        lastNotifyMillis.put(player.getUniqueId(), now);
        player.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("freeze-enabled-message")));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isFrozen(player)) return;

        if (event.getTo() == null) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        event.setTo(event.getFrom());
        notifyFrozen(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isFrozen(player)) return;

        event.setCancelled(true);
        notifyFrozen(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isFrozen(player)) return;

        event.setCancelled(true);
        notifyFrozen(player);
    }
}

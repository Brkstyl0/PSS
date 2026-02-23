package me.brkstyl.pss.listeners;

import me.brkstyl.pss.PluginMain;
import me.brkstyl.pss.menus.PssMenuHolder;
import me.brkstyl.pss.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class MenuListener implements Listener {

    private final PluginMain plugin;

    public MenuListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();
        boolean clickedTop = rawSlot >= 0 && rawSlot < topSize;

        if (!(event.getView().getTopInventory().getHolder() instanceof PssMenuHolder holder)) {
            return;
        }

        // Always cancel any clicks while PSS menu is open (prevents taking/moving items, shift-click, etc.)
        event.setCancelled(true);

        if (!clickedTop) return;
        if (event.getCurrentItem() == null) return;

        if (holder.getType() == PssMenuHolder.MenuType.MAIN) {

            if (rawSlot == 11) {
                if (!(player.hasPermission("pss.admin") || player.hasPermission("pss.moderation"))) {
                    player.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
                    return;
                }
                plugin.getMenuHandler().openReportsMenu(player);
                return;
            }

            if (rawSlot == 13) {
                if (!(player.hasPermission("pss.admin") || player.hasPermission("pss.maintenance"))) {
                    player.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
                    return;
                }
                boolean currentState = plugin.getConfig().getBoolean("maintenance.enabled", false);
                boolean newState = !currentState;

                plugin.getConfig().set("maintenance.enabled", newState);
                plugin.saveConfig();

                plugin.getMenuHandler().openMainMenu(player);

                String msgKey = newState ? "maintenance-enabled" : "maintenance-disabled";
                Component prefix = ColorUtils.format(plugin.getLanguageManager().getMessage("prefix"));
                Component message = ColorUtils.format(plugin.getLanguageManager().getMessage(msgKey));

                player.sendMessage(prefix.append(message));
            }

            if (rawSlot == 15) {
                if (!(player.hasPermission("pss.admin") || player.hasPermission("pss.maintenance"))) {
                    player.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
                    return;
                }

                boolean currentState = plugin.getConfig().getBoolean("chat.enabled", true);
                boolean newState = !currentState;

                plugin.getConfig().set("chat.enabled", newState);
                plugin.saveConfig();

                plugin.getMenuHandler().openMainMenu(player);

                String msgKey = newState ? "chat-enabled-broadcast" : "chat-disabled-broadcast";
                Component prefix = ColorUtils.format(plugin.getLanguageManager().getMessage("prefix"));
                Component message = ColorUtils.format(plugin.getLanguageManager().getMessage(msgKey));

                Bukkit.broadcastMessage(ColorUtils.toLegacy(prefix.append(message)));
            }

            if (rawSlot == 17) {
                if (!(player.hasPermission("pss.admin") || player.hasPermission("pss.maintenance"))) {
                    player.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
                    return;
                }

                boolean currentState = plugin.getConfig().getBoolean("freeze.enabled", false);
                boolean newState = !currentState;

                plugin.getConfig().set("freeze.enabled", newState);
                plugin.saveConfig();

                plugin.getMenuHandler().openMainMenu(player);

                String msgKey = newState ? "freeze-enabled-broadcast" : "freeze-disabled-broadcast";
                Component prefix = ColorUtils.format(plugin.getLanguageManager().getMessage("prefix"));
                Component message = ColorUtils.format(plugin.getLanguageManager().getMessage(msgKey));

                Bukkit.broadcastMessage(ColorUtils.toLegacy(prefix.append(message)));
            }
        }

        if (holder.getType() == PssMenuHolder.MenuType.REPORTS) {
            if (!(player.hasPermission("pss.admin") || player.hasPermission("pss.moderation"))) {
                player.sendMessage(ColorUtils.format(plugin.getLanguageManager().getMessage("no-permission")));
                return;
            }

            if (rawSlot < 45) {
                ItemStack item = event.getCurrentItem();
                if (item.getType() == Material.PLAYER_HEAD) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        PersistentDataContainer pdc = meta.getPersistentDataContainer();

                        String raw = pdc.get(plugin.getMenuHandler().getReportTargetKey(), PersistentDataType.STRING);
                        if (raw != null) {
                            try {
                                UUID targetId = UUID.fromString(raw);
                                plugin.getSpectateManager().startSpectate(player, targetId);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
                return;
            }

            if (rawSlot == 49) {
                plugin.getMenuHandler().openMainMenu(player);
            }
        }
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof PssMenuHolder)) return;

        int topSize = event.getView().getTopInventory().getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= 0 && rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
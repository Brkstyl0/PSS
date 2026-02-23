package me.brkstyl.pss.menus;

import me.brkstyl.pss.PluginMain;
import me.brkstyl.pss.menus.PssMenuHolder.MenuType;
import me.brkstyl.pss.reports.ReportEntry;
import me.brkstyl.pss.utils.ColorUtils;
import me.brkstyl.pss.utils.TimeUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MenuHandler {

    private final PluginMain plugin;
    private final NamespacedKey reportTargetKey;

    public MenuHandler(PluginMain plugin) {
        this.plugin = plugin;
        this.reportTargetKey = new NamespacedKey(plugin, "report_target");
    }

    public NamespacedKey getReportTargetKey() {
        return reportTargetKey;
    }

    public void openMainMenu(Player player) {
        String title = plugin.getLanguageManager().getMessage("gui-title");
        Inventory gui = Bukkit.createInventory(new PssMenuHolder(MenuType.MAIN), 27, ColorUtils.formatToLegacy(title));

        boolean isEnabled = plugin.getConfig().getBoolean("maintenance.enabled", false);
        ItemStack maintenanceItem = new ItemStack(isEnabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = maintenanceItem.getItemMeta();

        if (meta != null) {
            String statusKey = isEnabled ? "gui-maintenance-on" : "gui-maintenance-off";
            meta.displayName(ColorUtils.format(plugin.getLanguageManager().getMessage(statusKey)));

            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtils.format(plugin.getLanguageManager().getMessage("gui-maintenance-lore")));
            meta.lore(lore);

            maintenanceItem.setItemMeta(meta);
        }

        boolean chatEnabled = plugin.getConfig().getBoolean("chat.enabled", true);
        ItemStack chatItem = new ItemStack(chatEnabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta chatMeta = chatItem.getItemMeta();
        if (chatMeta != null) {
            String statusKey = chatEnabled ? "gui-chat-on" : "gui-chat-off";
            chatMeta.displayName(ColorUtils.format(plugin.getLanguageManager().getMessage(statusKey)));

            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtils.format(plugin.getLanguageManager().getMessage("gui-chat-lore")));
            chatMeta.lore(lore);

            chatItem.setItemMeta(chatMeta);
        }

        boolean freezeEnabled = plugin.getConfig().getBoolean("freeze.enabled", false);
        ItemStack freezeItem = new ItemStack(freezeEnabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta freezeMeta = freezeItem.getItemMeta();
        if (freezeMeta != null) {
            String statusKey = freezeEnabled ? "gui-freeze-on" : "gui-freeze-off";
            freezeMeta.displayName(ColorUtils.format(plugin.getLanguageManager().getMessage(statusKey)));

            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtils.format(plugin.getLanguageManager().getMessage("gui-freeze-lore")));
            freezeMeta.lore(lore);

            freezeItem.setItemMeta(freezeMeta);
        }

        ItemStack reportsItem = new ItemStack(Material.PAPER);
        ItemMeta reportsMeta = reportsItem.getItemMeta();
        if (reportsMeta != null) {
            reportsMeta.displayName(ColorUtils.format(plugin.getLanguageManager().getMessage("gui-reports-button")));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtils.format(plugin.getLanguageManager().getMessage("gui-reports-button-lore")));
            reportsMeta.lore(lore);
            reportsItem.setItemMeta(reportsMeta);
        }

        gui.setItem(11, reportsItem);
        gui.setItem(13, maintenanceItem);
        gui.setItem(15, chatItem);
        gui.setItem(17, freezeItem);
        player.openInventory(gui);
    }

    public void openReportsMenu(Player player) {
        String title = plugin.getLanguageManager().getMessage("gui-reports-title");
        Inventory gui = Bukkit.createInventory(new PssMenuHolder(MenuType.REPORTS), 54, ColorUtils.formatToLegacy(title));

        String lang = plugin.getConfig().getString("language", "en");
        long now = System.currentTimeMillis();

        List<ReportEntry> reports = plugin.getReportService().getSortedByLastReportDesc();
        int slot = 0;
        for (ReportEntry entry : reports) {
            if (slot >= 45) break; // keep bottom row for controls

            OfflinePlayer reported = Bukkit.getOfflinePlayer(entry.getReportedUuid());
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(reported);
                String name = entry.getLastKnownName() != null ? entry.getLastKnownName() : "Unknown";
                skullMeta.displayName(ColorUtils.format("<yellow>" + name));

                List<Component> lore = new ArrayList<>();
                String reportersLine = plugin.getLanguageManager().getMessage("gui-reports-lore-reporters")
                        .replace("<count>", String.valueOf(entry.getReporterCount()));
                lore.add(ColorUtils.format(reportersLine));
                String ago = TimeUtils.formatAgo(Math.max(0, now - entry.getLastReportedAtMillis()), lang);
                String lastLine = plugin.getLanguageManager().getMessage("gui-reports-lore-last")
                        .replace("<time>", ago);
                lore.add(ColorUtils.format(lastLine));
                skullMeta.lore(lore);

                PersistentDataContainer pdc = skullMeta.getPersistentDataContainer();
                pdc.set(reportTargetKey, PersistentDataType.STRING, entry.getReportedUuid().toString());
                head.setItemMeta(skullMeta);
            }
            gui.setItem(slot++, head);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(ColorUtils.format(plugin.getLanguageManager().getMessage("gui-back")));
            back.setItemMeta(backMeta);
        }
        gui.setItem(49, back);

        player.openInventory(gui);
    }
}
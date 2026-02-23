package me.brkstyl.pss.spectate;

import me.brkstyl.pss.PluginMain;
import me.brkstyl.pss.reports.ReportEntry;
import me.brkstyl.pss.reports.ReportService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SpectateManager {
    private final PluginMain plugin;

    private final Map<UUID, SpectateSession> sessions = new HashMap<>();

    private final ItemStack banItem;
    private final ItemStack warnItem;
    private final ItemStack relaxItem;
    private final ItemStack teleportItem;

    public SpectateManager(PluginMain plugin) {
        this.plugin = plugin;

        String lang = plugin.getConfig().getString("language", "en");
        boolean tr = lang.equalsIgnoreCase("tr");

        String banName = tr ? ChatColor.RED + "Banla" : ChatColor.RED + "Ban";
        String warnName = tr ? ChatColor.YELLOW + "Uyar" : ChatColor.YELLOW + "Warn";
        String relaxName = tr ? ChatColor.GREEN + "Rahat bırak" : ChatColor.GREEN + "Mark as clean";
        String tpName = tr ? ChatColor.AQUA + "Oyuncuya Işınlan" : ChatColor.AQUA + "Teleport to player";

        this.banItem = createItem(Material.REDSTONE, banName);
        this.warnItem = createItem(Material.PAPER, warnName);
        this.relaxItem = createItem(Material.LIME_DYE, relaxName);
        this.teleportItem = createItem(Material.COMPASS, tpName);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack getBanItem() {
        return banItem.clone();
    }

    public ItemStack getWarnItem() {
        return warnItem.clone();
    }

    public ItemStack getRelaxItem() {
        return relaxItem.clone();
    }

    public ItemStack getTeleportItem() {
        return teleportItem.clone();
    }

    public boolean isSpectating(Player staff) {
        return sessions.containsKey(staff.getUniqueId());
    }

    public SpectateSession getSession(Player staff) {
        return sessions.get(staff.getUniqueId());
    }

    public void startSpectate(Player staff, UUID targetId) {
        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            String lang = plugin.getConfig().getString("language", "en");
            String msg = lang.equalsIgnoreCase("tr")
                    ? ChatColor.RED + "Oyuncu çevrimdışı, izleme başlatılamadı."
                    : ChatColor.RED + "Player is offline, cannot start spectate.";
            staff.sendMessage(msg);
            return;
        }

        if (isSpectating(staff)) {
            String lang = plugin.getConfig().getString("language", "en");
            String msg = lang.equalsIgnoreCase("tr")
                    ? ChatColor.RED + "Zaten bir oyuncuyu izliyorsun."
                    : ChatColor.RED + "You are already spectating a player.";
            staff.sendMessage(msg);
            return;
        }

        SpectateSession session = new SpectateSession(staff, target);
        sessions.put(staff.getUniqueId(), session);

        // Freecam tarzı izleme: görünmez, uçabilen, oyuncunun biraz üzerinde
        staff.teleport(target.getLocation().add(0, 1.5, 0));
        staff.setGameMode(GameMode.SURVIVAL);
        staff.setAllowFlight(true);
        staff.setFlying(true);
        staff.setInvisible(true);
        staff.setCollidable(false);
        staff.setInvulnerable(true);

        // Staff İnvs
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(staff)) {
                online.hidePlayer(plugin, staff);
            }
        }

        // clear
        staff.getInventory().clear();
        staff.getInventory().setArmorContents(null);
        staff.getInventory().setItemInOffHand(null);

        staff.getInventory().setItem(0, getBanItem());
        staff.getInventory().setItem(3, getTeleportItem());
        staff.getInventory().setItem(4, getWarnItem());
        staff.getInventory().setItem(8, getRelaxItem());

        String lang = plugin.getConfig().getString("language", "en");
        if (lang.equalsIgnoreCase("tr")) {
            staff.sendMessage(ChatColor.GRAY + "Şu anda " + target.getName() + " oyuncusunu izliyorsun.");
            staff.sendMessage(ChatColor.DARK_GRAY + "Slot 1: Banla, Slot 4: Işınlan, Slot 5: Uyar, Slot 9: Rahat bırak.");
        } else {
            staff.sendMessage(ChatColor.GRAY + "You are now spectating " + target.getName() + ".");
            staff.sendMessage(ChatColor.DARK_GRAY + "Slot 1: Ban, Slot 4: Teleport, Slot 5: Warn, Slot 9: Mark as clean.");
        }
    }

    public void stopSpectate(Player staff) {
        SpectateSession session = sessions.remove(staff.getUniqueId());
        if (session != null) {
            staff.getInventory().setContents(session.getContents());
            staff.getInventory().setArmorContents(session.getArmor());
            staff.getInventory().setItemInOffHand(session.getOffHand());

            staff.setGameMode(session.getPreviousGameMode());
            staff.setAllowFlight(session.wasPreviousAllowFlight());
            staff.setFlying(session.wasPreviousFlying());
            staff.teleport(session.getPreviousLocation());
        }

        resetVisualState(staff);

        String lang = plugin.getConfig().getString("language", "en");
        String msg = lang.equalsIgnoreCase("tr")
                ? ChatColor.GRAY + "İzleme modu kapatıldı."
                : ChatColor.GRAY + "Spectate mode disabled.";
        staff.sendMessage(msg);
    }

    private void resetVisualState(Player staff) {
        staff.setInvisible(false);
        staff.setCollidable(true);
        staff.setInvulnerable(false);
        staff.removePotionEffect(PotionEffectType.INVISIBILITY);

        // Staff'i tüm oyunculara tekrar göster
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(staff)) {
                online.showPlayer(plugin, staff);
            }
        }
    }

    public void notifyReporters(UUID targetId, String message) {
        ReportService reportService = plugin.getReportService();
        ReportEntry entry = reportService.getEntry(targetId);
        if (entry == null) return;

        for (UUID reporterId : entry.getReporters()) {
            Player reporter = Bukkit.getPlayer(reporterId);
            if (reporter != null && reporter.isOnline()) {
                reporter.sendMessage(message);
            }
        }
    }
}

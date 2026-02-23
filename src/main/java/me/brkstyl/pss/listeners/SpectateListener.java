package me.brkstyl.pss.listeners;

import me.brkstyl.pss.PluginMain;
import me.brkstyl.pss.spectate.SpectateManager;
import me.brkstyl.pss.spectate.SpectateSession;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpectateListener implements Listener {

    private final PluginMain plugin;

    public SpectateListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    private SpectateManager manager() {
        return plugin.getSpectateManager();
    }

    // Spectate modundayken yetkili hasar almasın (çift güvence)
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (manager().isSpectating(player)) {
            event.setCancelled(true);
        }
    }

    // Spectate sırasında sunucuya yeni bağlanan oyunculardan da gizle
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        for (org.bukkit.entity.Player online : plugin.getServer().getOnlinePlayers()) {
            if (manager().isSpectating(online)) {
                joined.hidePlayer(plugin, online);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (manager().isSpectating(player)) {
            manager().stopSpectate(player);
        }

        // Eğer izlenen oyuncu çıkarsa bütün staff izlemeyi bırakır
        UUID targetId = player.getUniqueId();
        plugin.getServer().getOnlinePlayers().forEach(p -> {
            SpectateSession session = manager().getSession(p);
            if (session != null && session.getTargetId().equals(targetId)) {
                manager().stopSpectate(p);
            }
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!manager().isSpectating(player)) return;
        if (!(player.hasPermission("pss.admin") || player.hasPermission("pss.moderation"))) {
            manager().stopSpectate(player);
            return;
        }

        if (event.getItem() == null) return;

        event.setCancelled(true);

        SpectateSession session = manager().getSession(player);
        if (session == null) return;

        String lang = plugin.getConfig().getString("language", "en");
        boolean tr = lang.equalsIgnoreCase("tr");

        switch (event.getItem().getType()) {
            case REDSTONE -> {
                session.setState(SpectateSession.ConversationState.WAITING_BAN_DURATION);
                String msg = tr
                        ? "Ban süresini yaz (örnek: 7d, 3h, 30m, perm)."
                        : "Type ban duration (e.g. 7d, 3h, 30m, perm).";
                player.sendMessage(ChatColor.YELLOW + msg);
            }
            case PAPER -> {
                session.setState(SpectateSession.ConversationState.WAITING_WARN_MESSAGE);
                String msg = tr
                        ? "Oyuncuya gönderilecek uyarı mesajını yaz."
                        : "Type the warning message to send to the player.";
                player.sendMessage(ChatColor.YELLOW + msg);
            }
            case LIME_DYE -> {
                Player target = Bukkit.getPlayer(session.getTargetId());
                if (target != null) {
                    String msg = tr
                            ? ChatColor.GRAY + "[PSS] " + ChatColor.GREEN + target.getName() + " incelendi, hile tespit edilmedi."
                            : ChatColor.GRAY + "[PSS] " + ChatColor.GREEN + target.getName() + " was reviewed, no cheat detected.";
                    manager().notifyReporters(session.getTargetId(), msg);
                }
                // Rapor kaydını temizle
                try {
                    plugin.getReportService().clearReportsFor(session.getTargetId());
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to clear reports for player " + session.getTargetId() + ": " + e.getMessage());
                }
                // Her ihtimale karşı görünmezliği de sıfırla
                manager().stopSpectate(player);
            }
            case COMPASS -> {
                Player target = Bukkit.getPlayer(session.getTargetId());
                if (target != null) {
                    player.teleport(target.getLocation().add(0, 1.5, 0));
                    String msg = tr
                            ? ChatColor.GRAY + "Oyuncuya ışınlandın."
                            : ChatColor.GRAY + "Teleported to player.";
                    player.sendMessage(msg);
                }
            }
            default -> {
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!manager().isSpectating(player)) return;
        if (!(player.hasPermission("pss.admin") || player.hasPermission("pss.moderation"))) return;

        SpectateSession session = manager().getSession(player);
        if (session == null) return;

        SpectateSession.ConversationState state = session.getState();
        if (state == SpectateSession.ConversationState.NONE) return;

        event.setCancelled(true);

        String msg = event.getMessage();
        UUID staffId = player.getUniqueId();
        UUID targetId = session.getTargetId();

        if (state == SpectateSession.ConversationState.WAITING_BAN_DURATION) {
            session.setPendingBanDuration(msg);
            session.setState(SpectateSession.ConversationState.WAITING_BAN_REASON);
            String txt = plugin.getConfig().getString("language", "en").equalsIgnoreCase("tr")
                    ? "Ban sebebini yaz."
                    : "Type the ban reason.";
            player.sendMessage(ChatColor.YELLOW + txt);
            return;
        }

        if (state == SpectateSession.ConversationState.WAITING_BAN_REASON) {
            session.setState(SpectateSession.ConversationState.NONE);

            String durationString = session.getPendingBanDuration();
            String reason = msg;

            long millis = parseDuration(durationString);
            Date expires = millis <= 0 ? null : new Date(System.currentTimeMillis() + millis);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Player syncStaff = Bukkit.getPlayer(staffId);
                Player syncTarget = Bukkit.getPlayer(targetId);
                String lang = plugin.getConfig().getString("language", "en");
                boolean tr = lang.equalsIgnoreCase("tr");

                String whenText;
                if (expires == null) {
                    whenText = tr ? "kalıcı ban" : "permanent ban";
                } else {
                    Instant instant = expires.toInstant();
                    ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
                    String formatted = zdt.format(fmt);
                    whenText = tr
                            ? "kalkış zamanı: " + formatted
                            : "unban at: " + formatted;
                }

                if (syncTarget != null) {
                    String banMsg = (tr
                            ? ChatColor.RED + "Sunucudan banlandın.\n"
                            + ChatColor.GRAY + "Sebep: " + ChatColor.WHITE + reason + "\n"
                            + ChatColor.GRAY + whenText
                            : ChatColor.RED + "You are banned from this server.\n"
                            + ChatColor.GRAY + "Reason: " + ChatColor.WHITE + reason + "\n"
                            + ChatColor.GRAY + whenText);

                    BanList list = Bukkit.getBanList(BanList.Type.NAME);
                    list.addBan(syncTarget.getName(), banMsg, expires, syncStaff != null ? syncStaff.getName() : "PSS");
                    syncTarget.kickPlayer(banMsg);
                }

                String namePart = syncTarget != null ? syncTarget.getName() : (tr ? "Oyuncu" : "Player");
                String info = ChatColor.GRAY + "[PSS] " + ChatColor.RED + namePart
                        + (tr ? " banlandı. Sebep: " : " was banned. Reason: ") + reason
                        + ChatColor.GRAY + " (" + whenText + ")";
                manager().notifyReporters(targetId, info);

                if (syncStaff != null) {
                    String confirm = tr ? "Ban işlemi uygulandı." : "Ban applied successfully.";
                    syncStaff.sendMessage(ChatColor.GREEN + confirm);
                    manager().stopSpectate(syncStaff);
                }
            });
            return;
        }

        if (state == SpectateSession.ConversationState.WAITING_WARN_MESSAGE) {
            session.setState(SpectateSession.ConversationState.NONE);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Player syncStaff = Bukkit.getPlayer(staffId);
                Player syncTarget = Bukkit.getPlayer(targetId);
                String lang = plugin.getConfig().getString("language", "en");
                boolean tr = lang.equalsIgnoreCase("tr");

                if (syncTarget != null && syncTarget.isOnline()) {
                    String prefix = tr ? "UYARI: " : "WARNING: ";
                    syncTarget.sendMessage(ChatColor.GRAY + "[PSS] " + ChatColor.RED + prefix + ChatColor.YELLOW + msg);
                }

                String info = tr
                        ? ChatColor.GRAY + "[PSS] " + ChatColor.YELLOW
                        + (syncTarget != null ? syncTarget.getName() : "Oyuncu") + " uyarıldı: " + msg
                        : ChatColor.GRAY + "[PSS] " + ChatColor.YELLOW
                        + (syncTarget != null ? syncTarget.getName() : "Player") + " was warned: " + msg;
                manager().notifyReporters(targetId, info);

                if (syncStaff != null) {
                    String confirm = tr ? "Uyarı gönderildi." : "Warning sent.";
                    syncStaff.sendMessage(ChatColor.GREEN + confirm);
                }
            });
            return;
        }
    }

    private long parseDuration(String input) {
        input = input.trim().toLowerCase();
        if (input.equals("perm") || input.equals("perma") || input.equals("permanent")) {
            return -1;
        }

        long totalMillis = 0;
        StringBuilder number = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                if (number.length() == 0) continue;
                long value = Long.parseLong(number.toString());
                number.setLength(0);
                switch (c) {
                    case 'd' -> totalMillis += TimeUnit.DAYS.toMillis(value);
                    case 'h' -> totalMillis += TimeUnit.HOURS.toMillis(value);
                    case 'm' -> totalMillis += TimeUnit.MINUTES.toMillis(value);
                    case 's' -> totalMillis += TimeUnit.SECONDS.toMillis(value);
                    default -> {
                    }
                }
            }
        }
        return totalMillis;
    }
}
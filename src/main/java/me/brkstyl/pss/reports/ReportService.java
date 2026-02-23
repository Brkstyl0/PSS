package me.brkstyl.pss.reports;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

public class ReportService {
    private final ReportStore store;
    private final Map<UUID, ReportEntry> entries = new HashMap<>();

    public ReportService(ReportStore store) {
        this.store = store;
        reloadFromDisk();
    }

    public synchronized void reloadFromDisk() {
        store.load();
        entries.clear();
        entries.putAll(store.readAll());
    }

    public synchronized void flushToDisk() throws IOException {
        store.writeAll(entries);
        store.save();
    }

    public synchronized void report(Player reporter, OfflinePlayer reported, long nowMillis) throws IOException {
        UUID reportedUuid = reported.getUniqueId();
        String reportedName = reported.getName() != null ? reported.getName() : "Unknown";

        ReportEntry entry = entries.computeIfAbsent(reportedUuid, uuid -> new ReportEntry(uuid, reportedName));
        entry.setLastKnownName(reportedName);
        entry.getReporters().add(reporter.getUniqueId());
        entry.setLastReportedAtMillis(nowMillis);

        flushToDisk();
    }

    public synchronized List<ReportEntry> getSortedByLastReportDesc() {
        return entries.values().stream()
                .sorted(Comparator.comparingLong(ReportEntry::getLastReportedAtMillis).reversed())
                .toList();
    }

    public synchronized ReportEntry getEntry(UUID uuid) {
        return entries.get(uuid);
    }

    public synchronized void clearReportsFor(UUID uuid) throws IOException {
        if (entries.remove(uuid) != null) {
            flushToDisk();
        }
    }
}

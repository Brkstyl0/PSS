package me.brkstyl.pss.reports;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReportStore {
    private final File file;
    private FileConfiguration config;

    public ReportStore(File dataFolder) {
        this.file = new File(dataFolder, "reports.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void load() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() throws IOException {
        config.save(file);
    }

    public Map<UUID, ReportEntry> readAll() {
        Map<UUID, ReportEntry> out = new HashMap<>();
        ConfigurationSection root = config.getConfigurationSection("reports");
        if (root == null) return out;

        for (String key : root.getKeys(false)) {
            UUID reportedUuid;
            try {
                reportedUuid = UUID.fromString(key);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            String base = "reports." + key + ".";
            String name = config.getString(base + "name", "Unknown");
            long lastAt = config.getLong(base + "lastReportedAt", 0L);
            List<String> reporterStrings = config.getStringList(base + "reporters");

            ReportEntry entry = new ReportEntry(reportedUuid, name);
            entry.setLastReportedAtMillis(lastAt);
            for (String rs : reporterStrings) {
                try {
                    entry.getReporters().add(UUID.fromString(rs));
                } catch (IllegalArgumentException ignored) {
                    // skip
                }
            }
            out.put(reportedUuid, entry);
        }

        return out;
    }

    public void writeAll(Map<UUID, ReportEntry> entries) {
        config.set("reports", null);
        for (ReportEntry entry : entries.values()) {
            String key = entry.getReportedUuid().toString();
            String base = "reports." + key + ".";
            config.set(base + "name", entry.getLastKnownName());
            config.set(base + "lastReportedAt", entry.getLastReportedAtMillis());
            List<String> reporters = entry.getReporters().stream().map(UUID::toString).toList();
            config.set(base + "reporters", reporters);
        }
    }
}

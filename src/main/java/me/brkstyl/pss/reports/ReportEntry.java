package me.brkstyl.pss.reports;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class ReportEntry {
    private final UUID reportedUuid;
    private String lastKnownName;
    private final Set<UUID> reporters = new LinkedHashSet<>();
    private long lastReportedAtMillis;

    public ReportEntry(UUID reportedUuid, String lastKnownName) {
        this.reportedUuid = reportedUuid;
        this.lastKnownName = lastKnownName;
    }

    public UUID getReportedUuid() {
        return reportedUuid;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public Set<UUID> getReporters() {
        return reporters;
    }

    public int getReporterCount() {
        return reporters.size();
    }

    public long getLastReportedAtMillis() {
        return lastReportedAtMillis;
    }

    public void setLastReportedAtMillis(long lastReportedAtMillis) {
        this.lastReportedAtMillis = lastReportedAtMillis;
    }
}

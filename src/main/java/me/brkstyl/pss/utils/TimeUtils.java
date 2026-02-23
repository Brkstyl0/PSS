package me.brkstyl.pss.utils;

import java.time.Duration;

public class TimeUtils {
    private TimeUtils() {}

    public static String formatAgo(long agoMillis, String lang) {
        if (agoMillis <= 0) return lang.equalsIgnoreCase("tr") ? "bilinmiyor" : "unknown";

        Duration d = Duration.ofMillis(agoMillis);
        long seconds = d.getSeconds();

        if (seconds < 60) {
            return lang.equalsIgnoreCase("tr") ? seconds + " sn önce" : seconds + "s ago";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return lang.equalsIgnoreCase("tr") ? minutes + " dk önce" : minutes + "m ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return lang.equalsIgnoreCase("tr") ? hours + " sa önce" : hours + "h ago";
        }
        long days = hours / 24;
        return lang.equalsIgnoreCase("tr") ? days + " gün önce" : days + "d ago";
    }
}

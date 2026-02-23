package me.brkstyl.pss.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.inventory.InventoryView;

import java.lang.reflect.Method;

public final class InventoryTitleCompat {

    private InventoryTitleCompat() {
    }

    public static String getPlainTitle(InventoryView view) {
        if (view == null) return "";

        // 1.19.x: InventoryView#getTitle() -> String
        try {
            Method m = view.getClass().getMethod("getTitle");
            Object out = m.invoke(view);
            if (out instanceof String s) {
                String stripped = ChatColor.stripColor(s);
                return stripped != null ? stripped : "";
            }
        } catch (Throwable ignored) {
        }

        // 1.20.1+: InventoryView#title() -> Component
        try {
            Method m = view.getClass().getMethod("title");
            Object out = m.invoke(view);
            if (out instanceof Component c) {
                return PlainTextComponentSerializer.plainText().serialize(c);
            }
        } catch (Throwable ignored) {
        }

        return "";
    }
}

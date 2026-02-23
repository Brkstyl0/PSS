package me.brkstyl.pss.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ColorUtils {
    //mini messages
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

    public static Component format(String message) {
        return mm.deserialize(message);
    }

    public static String toLegacy(Component component) {
        return legacy.serialize(component);
    }

    public static String formatToLegacy(String message) {
        return toLegacy(format(message));
    }

    public static String toPlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
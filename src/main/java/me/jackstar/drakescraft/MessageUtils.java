package me.jackstar.drakescraft.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public class MessageUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static Component parseMini(String text) {
        if (text == null) return Component.empty();
        return MINI_MESSAGE.deserialize(text);
    }

    public static Component parseLegacy(String text) {
        if (text == null) return Component.empty();
        return LEGACY_SERIALIZER.deserialize(text);
    }

    public static Component parse(String text) {
        if (text == null) return Component.empty();
        if (text.contains("&")) {
            return parseLegacy(text);
        }
        return parseMini(text);
    }

    public static void send(CommandSender sender, String text) {
        if (sender != null) {
            sender.sendMessage(parse(text));
        }
    }

    public static void send(CommandSender sender, Component component) {
        if (sender != null && component != null) {
            sender.sendMessage(component);
        }
    }

    public static String color(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }
}

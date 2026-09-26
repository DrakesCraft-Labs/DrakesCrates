package me.jackstar.drakescrates.compat;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Robust Slimefun Hook using reflection.
 * Compatible with upstream Slimefun4 and DrakesCraft fork.
 * Safe when Slimefun is absent (standalone mode).
 */
public final class SlimefunHook {

    private static final Logger LOGGER = Logger.getLogger("DrakesCrates-Slimefun");

    private static final String[] SLIMEFUN_ITEM_CLASSES = {
            "com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem",
            "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem"
    };

    private static final Set<String> SLIMEFUN_NAMESPACES = Set.of(
            "slimefun",
            "cultivation",
            "slimeframe",
            "dynatech",
            "exoticgarden",
            "alchimiavitae",
            "infinityexpansion",
            "fluffymachines",
            "sensibletoolbox",
            "extraweapons",
            "villagertrade",
            "sfcalc",
            "slimybees",
            "slimytreetaps",
            "equivalencytech"
    );

    private static boolean initialized = false;
    private static boolean available = false;
    private static Method getByIdMethod;
    private static Method getByItemMethod;
    private static Method getItemMethod;
    private static Method getIdMethod;

    private SlimefunHook() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        if (Bukkit.getServer() == null) return;
        if (Bukkit.getPluginManager().getPlugin("Slimefun") == null
                && Bukkit.getPluginManager().getPlugin("Slimefun4") == null
                && Bukkit.getPluginManager().getPlugin("Slimefun4-Drake") == null) {
            LOGGER.info("[SlimefunHook] Slimefun plugin not detected. Running in standalone mode.");
            return;
        }

        for (String className : SLIMEFUN_ITEM_CLASSES) {
            try {
                Class<?> clazz = Class.forName(className);
                getByIdMethod = clazz.getMethod("getById", String.class);
                getByItemMethod = clazz.getMethod("getByItem", ItemStack.class);
                getItemMethod = clazz.getMethod("getItem");
                try {
                    getIdMethod = clazz.getMethod("getId");
                } catch (NoSuchMethodException ignored) {}
                available = true;
                LOGGER.info("[SlimefunHook] Successfully hooked into Slimefun via " + className);
                return;
            } catch (ClassNotFoundException ignored) {
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[SlimefunHook] Error hooking into " + className, e);
            }
        }

        LOGGER.warning("[SlimefunHook] Slimefun plugin was present but API classes could not be resolved.");
    }

    public static boolean isAvailable() {
        if (!initialized) init();
        return available;
    }

    /**
     * Resolves a Slimefun item by its ID.
     * Returns null if not found or Slimefun is absent.
     */
    public static ItemStack getSlimefunItem(String id, int amount) {
        if (!isAvailable() || id == null || id.isBlank()) return null;
        try {
            Object sfItem = getByIdMethod.invoke(null, id.trim().toUpperCase(Locale.ROOT));
            if (sfItem == null) {
                sfItem = getByIdMethod.invoke(null, id.trim());
            }
            if (sfItem != null && getItemMethod != null) {
                ItemStack stack = (ItemStack) getItemMethod.invoke(sfItem);
                if (stack != null) {
                    ItemStack clone = stack.clone();
                    clone.setAmount(Math.max(1, amount));
                    return clone;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[SlimefunHook] Failed to get Slimefun item: " + id, e);
        }
        return null;
    }

    /**
     * Checks if an ItemStack is a Slimefun item or belongs to a Slimefun addon namespace.
     */
    public static boolean isSlimefunItem(ItemStack item) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) return false;
        if (Bukkit.getServer() == null) return false;

        // 1. Try Slimefun API if available
        if (isAvailable() && getByItemMethod != null) {
            try {
                Object sfItem = getByItemMethod.invoke(null, item);
                if (sfItem != null) return true;
            } catch (Exception ignored) {}
        }

        // 2. Check PersistentDataContainer namespaces
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            for (NamespacedKey key : pdc.getKeys()) {
                String ns = key.getNamespace().toLowerCase(Locale.ROOT);
                if (SLIMEFUN_NAMESPACES.contains(ns)) {
                    return true;
                }
                if (key.getKey().toLowerCase(Locale.ROOT).contains("slimefun")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Extracts Slimefun ID from an ItemStack if present.
     */
    public static String getSlimefunId(ItemStack item) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) return null;
        if (isAvailable() && getByItemMethod != null && getIdMethod != null) {
            try {
                Object sfItem = getByItemMethod.invoke(null, item);
                if (sfItem != null) {
                    return (String) getIdMethod.invoke(sfItem);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}

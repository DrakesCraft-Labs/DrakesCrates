package me.jackstar.drakescrates.domain.models;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class Key {
    private final String id;
    private final Component displayName;
    private final ItemStack item; // The physical item representing the key

    public Key(String id, Component displayName, ItemStack item) {
        this.id = id;
        this.displayName = displayName;
        this.item = item;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public ItemStack getItem() {
        return item.clone();
    } // Return clone to prevent modification
}

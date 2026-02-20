package me.jackstar.drakescrates.domain.models;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class Reward {
    private final String id;
    private final Component displayName;
    private final double chance; // Percentage chance (0-100)
    private final List<String> commands; // Commands to execute on win
    private final ItemStack displayItem; // Item shown in preview/GUI

    public Reward(String id, Component displayName, double chance, List<String> commands, ItemStack displayItem) {
        this.id = id;
        this.displayName = displayName;
        this.chance = chance;
        this.commands = commands;
        this.displayItem = displayItem;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public double getChance() {
        return chance;
    }

    public List<String> getCommands() {
        return commands;
    }

    public ItemStack getDisplayItem() {
        return displayItem.clone();
    }
}

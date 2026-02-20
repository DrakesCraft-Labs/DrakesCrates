package me.jackstar.drakescrates.domain.models;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class Crate {
    private final String id;
    private final Component displayName;
    private final CrateType type;
    private final List<Reward> rewards;
    private final ItemStack previewItem;
    private final List<Location> locations;

    public Crate(String id, Component displayName, CrateType type, List<Reward> rewards, ItemStack previewItem,
            List<Location> locations) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.rewards = rewards;
        this.previewItem = previewItem;
        this.locations = locations;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public CrateType getType() {
        return type;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public ItemStack getPreviewItem() {
        return previewItem;
    }

    public List<Location> getLocations() {
        return locations;
    }
}

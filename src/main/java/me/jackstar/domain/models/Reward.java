package me.jackstar.drakescrates.domain.models;

import me.jackstar.drakescrates.compat.SlimefunHook;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class Reward {
    private final String id;
    private final Component displayName;
    private final double chance; // Percentage chance (0-100)
    private final List<String> commands; // Commands to execute on win
    private final ItemStack displayItem; // Item shown in preview/GUI
    private final String slimefunId; // Optional Slimefun item ID
    private final boolean vanilla; // Flag for pure vanilla item reward
    private final int amount;

    public Reward(String id, Component displayName, double chance, List<String> commands,
                  ItemStack displayItem, String slimefunId, boolean vanilla, int amount) {
        this.id = id;
        this.displayName = displayName;
        this.chance = chance;
        this.commands = commands != null ? commands : List.of();
        this.displayItem = displayItem;
        this.slimefunId = (slimefunId != null && !slimefunId.isBlank()) ? slimefunId.trim() : null;
        this.vanilla = vanilla;
        this.amount = Math.max(1, amount);
    }

    public Reward(String id, Component displayName, double chance, List<String> commands, ItemStack displayItem) {
        this(id, displayName, chance, commands, displayItem, null, true,
                displayItem != null ? displayItem.getAmount() : 1);
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
        if (displayItem != null) {
            return displayItem.clone();
        }
        if (slimefunId != null) {
            ItemStack sfStack = SlimefunHook.getSlimefunItem(slimefunId, amount);
            if (sfStack != null) return sfStack;
        }
        return new ItemStack(org.bukkit.Material.CHEST);
    }

    public String getSlimefunId() {
        return slimefunId;
    }

    public boolean isVanilla() {
        return vanilla && !isSlimefun();
    }

    public int getAmount() {
        return amount;
    }

    public boolean isSlimefun() {
        if (slimefunId != null) return true;
        if (displayItem != null && SlimefunHook.isSlimefunItem(displayItem)) return true;
        if (commands != null) {
            for (String cmd : commands) {
                if (cmd != null) {
                    String lower = cmd.toLowerCase();
                    if (lower.contains("sf give") || lower.contains("slimefun give")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ItemStack createRewardItem() {
        if (slimefunId != null) {
            ItemStack sfStack = SlimefunHook.getSlimefunItem(slimefunId, amount);
            if (sfStack != null) return sfStack;
        }
        if (displayItem != null) {
            ItemStack clone = displayItem.clone();
            clone.setAmount(amount);
            return clone;
        }
        return null;
    }
}

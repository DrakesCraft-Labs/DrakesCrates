package me.jackstar.drakescrates.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jackstar.drakescrates.application.repositories.CrateRepository;
import me.jackstar.drakescrates.domain.models.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrakesCratesPlaceholderExpansion extends PlaceholderExpansion {

    private final CrateRepository crateRepository;

    public DrakesCratesPlaceholderExpansion(CrateRepository crateRepository) {
        this.crateRepository = crateRepository;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "drakescrates";
    }

    @Override
    public @NotNull String getAuthor() {
        return "JackStar";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "0";
        }

        if ("keys_physical".equalsIgnoreCase(params)) {
            return String.valueOf(countPhysicalKeys(player));
        }

        return null;
    }

    private int countPhysicalKeys(Player player) {
        int count = 0;
        for (Key key : crateRepository.getAllKeys()) {
            ItemStack target = key.getItem();
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack == null || stack.getType().isAir()) {
                    continue;
                }
                if (stack.isSimilar(target)) {
                    count += stack.getAmount();
                }
            }
        }
        return count;
    }
}

package me.jackstar.drakescrates.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jackstar.drakescrates.application.repositories.CrateRepository;
import me.jackstar.drakescrates.domain.models.Key;
import me.jackstar.drakescrates.oracle.OracleRepository;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrakesCratesPlaceholderExpansion extends PlaceholderExpansion {

    private final CrateRepository crateRepository;
    private final OracleRepository oracleRepository;

    public DrakesCratesPlaceholderExpansion(CrateRepository crateRepository, OracleRepository oracleRepository) {
        this.crateRepository = crateRepository;
        this.oracleRepository = oracleRepository;
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

        String lower = params.toLowerCase();
        if (lower.startsWith("oracle_keys_")) {
            return String.valueOf(oracleRepository.balance(player.getUniqueId(), params.substring("oracle_keys_".length())));
        }
        if (lower.startsWith("oracle_pity_")) {
            return String.valueOf(oracleRepository.pity(player.getUniqueId(), params.substring("oracle_pity_".length())));
        }

        if (params.toLowerCase().startsWith("keys_")) {
            String keyId = params.substring("keys_".length());
            if (keyId.isBlank()) {
                return "0";
            }
            return String.valueOf(countPhysicalKeys(player, keyId));
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

    private int countPhysicalKeys(Player player, String keyId) {
        Key key = crateRepository.findKeyById(keyId).orElse(null);
        if (key == null) {
            return 0;
        }

        int count = 0;
        ItemStack target = key.getItem();
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            if (stack.isSimilar(target)) {
                count += stack.getAmount();
            }
        }
        return count;
    }
}

package me.jackstar.drakescrates.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jackstar.drakescrates.application.repositories.CrateRepository;
import me.jackstar.drakescrates.application.repositories.VirtualKeyRepository;
import me.jackstar.drakescrates.domain.modality.ModalityManager;
import me.jackstar.drakescrates.domain.models.Key;
import me.jackstar.drakescrates.oracle.OracleRepository;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public class DrakesCratesPlaceholderExpansion extends PlaceholderExpansion {

    private final CrateRepository crateRepository;
    private final VirtualKeyRepository virtualKeyRepository;
    private final ModalityManager modalityManager;
    private final OracleRepository oracleRepository;

    public DrakesCratesPlaceholderExpansion(CrateRepository crateRepository,
                                           VirtualKeyRepository virtualKeyRepository,
                                           ModalityManager modalityManager,
                                           OracleRepository oracleRepository) {
        this.crateRepository = crateRepository;
        this.virtualKeyRepository = virtualKeyRepository;
        this.modalityManager = modalityManager;
        this.oracleRepository = oracleRepository;
    }

    public DrakesCratesPlaceholderExpansion(CrateRepository crateRepository, OracleRepository oracleRepository) {
        this(crateRepository, null, null, oracleRepository);
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
        return "2.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        String p = params.toLowerCase(Locale.ROOT);

        // Modality info
        if (p.equals("in_clasico")) {
            return modalityManager != null ? String.valueOf(modalityManager.isClasico(player)) : "false";
        }
        if (p.equals("modality")) {
            return modalityManager != null ? modalityManager.getCurrentModality(player).getId() : "global";
        }

        // Virtual key balance: %drakescrates_virtual_keys_<key_id>%
        if (p.startsWith("virtual_keys_") && virtualKeyRepository != null) {
            String keyId = p.substring("virtual_keys_".length());
            return String.valueOf(virtualKeyRepository.getBalance(player.getUniqueId(), keyId));
        }

        // Physical keys: %drakescrates_physical_keys_<key_id>%
        if (p.startsWith("physical_keys_") && crateRepository != null) {
            String keyId = p.substring("physical_keys_".length());
            Optional<Key> key = crateRepository.findKeyById(keyId);
            return String.valueOf(key.map(k -> countPhysicalKeys(player, k)).orElse(0));
        }

        // Total keys: %drakescrates_total_keys_<key_id>%
        if (p.startsWith("total_keys_")) {
            String keyId = p.substring("total_keys_".length());
            int virt = virtualKeyRepository != null ? virtualKeyRepository.getBalance(player.getUniqueId(), keyId) : 0;
            Optional<Key> key = crateRepository != null ? crateRepository.findKeyById(keyId) : Optional.empty();
            int phys = key.map(k -> countPhysicalKeys(player, k)).orElse(0);
            return String.valueOf(virt + phys);
        }

        // Oracle fallback: %drakescrates_oracle_keys_<relicary>%
        if (p.startsWith("oracle_keys_") && oracleRepository != null) {
            String relicary = p.substring("oracle_keys_".length());
            return String.valueOf(oracleRepository.balance(player.getUniqueId(), relicary));
        }

        return null;
    }

    private int countPhysicalKeys(Player player, Key key) {
        int count = 0;
        ItemStack target = key.getItem();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(target)) {
                count += item.getAmount();
            }
        }
        return count;
    }
}

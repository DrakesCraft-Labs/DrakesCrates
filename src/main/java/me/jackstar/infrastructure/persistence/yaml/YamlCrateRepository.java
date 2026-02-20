package me.jackstar.drakescrates.infrastructure.persistence.yaml;

import me.jackstar.drakescraft.utils.MessageUtils;
import me.jackstar.drakescrates.application.repositories.CrateRepository;
import me.jackstar.drakescrates.domain.models.Crate;
import me.jackstar.drakescrates.domain.models.CrateType;
import me.jackstar.drakescrates.domain.models.Key;
import me.jackstar.drakescrates.domain.models.Reward;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class YamlCrateRepository implements CrateRepository {

    private static final String FILE_NAME = "crates.yml";

    private final JavaPlugin plugin;

    private final Map<String, Crate> crates = new LinkedHashMap<>();
    private final Map<String, Key> keys = new LinkedHashMap<>();
    private final Map<String, String> requiredKeyByCrate = new HashMap<>();

    public YamlCrateRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        ensureDefaultFile();
        reload();
    }

    @Override
    public void reload() {
        crates.clear();
        keys.clear();
        requiredKeyByCrate.clear();

        final File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.getLogger().warning("crates.yml not found. No crates were loaded.");
            return;
        }

        final YamlConfiguration config;
        try {
            config = YamlConfiguration.loadConfiguration(file);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load crates.yml. Repository will stay empty.", ex);
            return;
        }

        loadKeys(config.getConfigurationSection("keys"));
        loadCrates(config.getConfigurationSection("crates"));
        plugin.getLogger().info("Loaded " + crates.size() + " crate(s) and " + keys.size() + " key definition(s).");
    }

    @Override
    public Collection<Crate> getAllCrates() {
        return Collections.unmodifiableCollection(crates.values());
    }

    @Override
    public Collection<Key> getAllKeys() {
        return Collections.unmodifiableCollection(keys.values());
    }

    @Override
    public Optional<Crate> findCrateById(String crateId) {
        return Optional.ofNullable(crates.get(normalizeId(crateId)));
    }

    @Override
    public Optional<Key> findKeyById(String keyId) {
        return Optional.ofNullable(keys.get(normalizeId(keyId)));
    }

    @Override
    public Optional<Key> findRequiredKeyForCrate(String crateId) {
        final String requiredKeyId = requiredKeyByCrate.get(normalizeId(crateId));
        if (requiredKeyId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(keys.get(requiredKeyId));
    }

    @Override
    public Optional<Crate> findCrateByLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return Optional.empty();
        }

        for (Crate crate : crates.values()) {
            List<Location> crateLocations = crate.getLocations();
            if (crateLocations == null || crateLocations.isEmpty()) {
                continue;
            }
            for (Location crateLocation : crateLocations) {
                if (sameBlock(crateLocation, location)) {
                    return Optional.of(crate);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean updateRewardChance(String crateId, String rewardId, double newChance) {
        String normalizedCrateId = normalizeId(crateId);
        String normalizedRewardId = normalizeId(rewardId);
        if (normalizedCrateId == null || normalizedRewardId == null || newChance <= 0.0D) {
            return false;
        }

        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            return false;
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection cratesSection = config.getConfigurationSection("crates");
            if (cratesSection == null) {
                return false;
            }

            String rawCrateKey = findRawKeyByNormalizedId(cratesSection, normalizedCrateId);
            if (rawCrateKey == null) {
                return false;
            }

            ConfigurationSection rewardsSection = config.getConfigurationSection("crates." + rawCrateKey + ".rewards");
            if (rewardsSection == null) {
                return false;
            }

            String rawRewardKey = findRawKeyByNormalizedId(rewardsSection, normalizedRewardId);
            if (rawRewardKey == null) {
                return false;
            }

            config.set("crates." + rawCrateKey + ".rewards." + rawRewardKey + ".chance", newChance);
            config.save(file);
            reload();
            return true;
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to update reward chance for crate '" + crateId + "', reward '" + rewardId + "'.",
                    ex);
            return false;
        }
    }

    private void loadKeys(ConfigurationSection keysSection) {
        if (keysSection == null) {
            plugin.getLogger().warning("No keys section found in crates.yml.");
            return;
        }

        for (String rawKeyId : keysSection.getKeys(false)) {
            final String keyId = normalizeId(rawKeyId);
            try {
                ConfigurationSection keySection = keysSection.getConfigurationSection(rawKeyId);
                if (keySection == null) {
                    plugin.getLogger().warning("Invalid key section: " + rawKeyId);
                    continue;
                }

                String displayName = keySection.getString("display-name", "<yellow>" + rawKeyId);
                ConfigurationSection itemSection = keySection.getConfigurationSection("item");
                ItemStack keyItem = parseItem(itemSection, "keys." + rawKeyId + ".item");

                Key key = new Key(keyId, MessageUtils.parse(displayName), keyItem);
                keys.put(keyId, key);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse key '" + rawKeyId + "'. Skipping.", ex);
            }
        }
    }

    private void loadCrates(ConfigurationSection cratesSection) {
        if (cratesSection == null) {
            plugin.getLogger().warning("No crates section found in crates.yml.");
            return;
        }

        for (String rawCrateId : cratesSection.getKeys(false)) {
            final String crateId = normalizeId(rawCrateId);
            try {
                ConfigurationSection crateSection = cratesSection.getConfigurationSection(rawCrateId);
                if (crateSection == null) {
                    plugin.getLogger().warning("Invalid crate section: " + rawCrateId);
                    continue;
                }

                String displayName = crateSection.getString("display-name", "<gold>" + rawCrateId);
                CrateType type = parseCrateType(crateSection.getString("type", "FREE"), rawCrateId);
                ItemStack previewItem = parseItem(crateSection.getConfigurationSection("preview-item"),
                        "crates." + rawCrateId + ".preview-item");

                List<Reward> rewards = parseRewards(crateSection.getConfigurationSection("rewards"), rawCrateId);
                if (rewards.isEmpty()) {
                    plugin.getLogger().warning("Crate '" + rawCrateId + "' has no valid rewards. Skipping.");
                    continue;
                }

                List<Location> locations = parseLocations(crateSection.getStringList("locations"), rawCrateId);
                String requiredKeyId = null;
                if (type == CrateType.PHYSICAL_KEY) {
                    requiredKeyId = normalizeId(crateSection.getString("key-id"));
                    if (requiredKeyId == null || !keys.containsKey(requiredKeyId)) {
                        plugin.getLogger().warning(
                                "Crate '" + rawCrateId + "' requires PHYSICAL_KEY but key-id is missing or invalid.");
                        continue;
                    }
                }

                Crate crate = new Crate(crateId, MessageUtils.parse(displayName), type, rewards, previewItem, locations);
                crates.put(crateId, crate);
                if (requiredKeyId != null) {
                    requiredKeyByCrate.put(crateId, requiredKeyId);
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse crate '" + rawCrateId + "'. Skipping.", ex);
            }
        }
    }

    private List<Reward> parseRewards(ConfigurationSection rewardsSection, String crateId) {
        if (rewardsSection == null) {
            return Collections.emptyList();
        }

        List<Reward> rewards = new ArrayList<>();
        for (String rawRewardId : rewardsSection.getKeys(false)) {
            try {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rawRewardId);
                if (rewardSection == null) {
                    plugin.getLogger().warning("Invalid reward section: crates." + crateId + ".rewards." + rawRewardId);
                    continue;
                }

                double chance = rewardSection.getDouble("chance", 0.0D);
                if (chance <= 0.0D) {
                    plugin.getLogger().warning("Reward '" + rawRewardId + "' in crate '" + crateId
                            + "' has non-positive chance. Skipping.");
                    continue;
                }

                String displayName = rewardSection.getString("display-name", "<green>" + rawRewardId);
                List<String> commands = rewardSection.getStringList("commands");
                ItemStack displayItem = parseItem(rewardSection.getConfigurationSection("display-item"),
                        "crates." + crateId + ".rewards." + rawRewardId + ".display-item");

                Reward reward = new Reward(
                        normalizeId(rawRewardId),
                        MessageUtils.parse(displayName),
                        chance,
                        commands,
                        displayItem);
                rewards.add(reward);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to parse reward '" + rawRewardId + "' in crate '" + crateId + "'. Skipping.", ex);
            }
        }
        return rewards;
    }

    private List<Location> parseLocations(List<String> serializedLocations, String crateId) {
        if (serializedLocations == null || serializedLocations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> locations = new ArrayList<>();
        for (String rawLocation : serializedLocations) {
            try {
                String[] parts = rawLocation.split(",");
                if (parts.length < 4) {
                    plugin.getLogger().warning("Invalid location for crate '" + crateId + "': " + rawLocation);
                    continue;
                }

                String worldName = parts[0].trim();
                if (Bukkit.getWorld(worldName) == null) {
                    plugin.getLogger().warning("Unknown world '" + worldName + "' for crate '" + crateId + "'.");
                    continue;
                }

                double x = Double.parseDouble(parts[1].trim());
                double y = Double.parseDouble(parts[2].trim());
                double z = Double.parseDouble(parts[3].trim());
                float yaw = parts.length > 4 ? Float.parseFloat(parts[4].trim()) : 0.0F;
                float pitch = parts.length > 5 ? Float.parseFloat(parts[5].trim()) : 0.0F;

                locations.add(new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch));
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to parse location '" + rawLocation + "' for crate '" + crateId + "'.", ex);
            }
        }
        return locations;
    }

    private CrateType parseCrateType(String rawType, String crateId) {
        try {
            return CrateType.valueOf(rawType.toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            plugin.getLogger().warning(
                    "Invalid crate type '" + rawType + "' for crate '" + crateId + "'. Defaulting to FREE.");
            return CrateType.FREE;
        }
    }

    private ItemStack parseItem(ConfigurationSection itemSection, String path) {
        if (itemSection == null) {
            return new ItemStack(Material.CHEST);
        }

        String materialName = itemSection.getString("material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("Invalid material '" + materialName + "' at " + path + ". Using BARRIER.");
            material = Material.BARRIER;
        }

        int amount = Math.max(1, itemSection.getInt("amount", 1));
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        String name = itemSection.getString("name");
        if (name != null && !name.isBlank()) {
            meta.displayName(MessageUtils.parse(name));
        }

        List<String> loreLines = itemSection.getStringList("lore");
        if (!loreLines.isEmpty()) {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(MessageUtils.parse(line));
            }
            meta.lore(lore);
        }

        ConfigurationSection enchantSection = itemSection.getConfigurationSection("enchantments");
        if (enchantSection != null) {
            for (String rawEnchant : enchantSection.getKeys(false)) {
                int level = enchantSection.getInt(rawEnchant, 1);
                Enchantment enchantment = parseEnchantment(rawEnchant);
                if (enchantment == null) {
                    plugin.getLogger().warning("Unknown enchantment '" + rawEnchant + "' at " + path + ".");
                    continue;
                }
                meta.addEnchant(enchantment, Math.max(level, 1), true);
            }
        }

        if (itemSection.contains("custom-model-data")) {
            meta.setCustomModelData(itemSection.getInt("custom-model-data"));
        }

        item.setItemMeta(meta);
        return item;
    }

    private Enchantment parseEnchantment(String rawName) {
        Enchantment byName = Enchantment.getByName(rawName.toUpperCase(Locale.ROOT));
        if (byName != null) {
            return byName;
        }

        NamespacedKey direct = NamespacedKey.fromString(rawName.toLowerCase(Locale.ROOT));
        if (direct != null) {
            Enchantment byDirectKey = Enchantment.getByKey(direct);
            if (byDirectKey != null) {
                return byDirectKey;
            }
        }

        NamespacedKey minecraftKey = NamespacedKey.minecraft(rawName.toLowerCase(Locale.ROOT));
        return Enchantment.getByKey(minecraftKey);
    }

    private void ensureDefaultFile() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder: " + plugin.getDataFolder().getAbsolutePath());
        }

        File cratesFile = new File(plugin.getDataFolder(), FILE_NAME);
        if (cratesFile.exists()) {
            return;
        }

        try {
            plugin.saveResource(FILE_NAME, false);
        } catch (IllegalArgumentException missingResource) {
            try {
                if (!cratesFile.createNewFile()) {
                    plugin.getLogger().warning("Could not create empty crates.yml file.");
                }
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create crates.yml.", ex);
            }
        }
    }

    private String normalizeId(String id) {
        if (id == null) {
            return null;
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean sameBlock(Location first, Location second) {
        if (first == null || second == null || first.getWorld() == null || second.getWorld() == null) {
            return false;
        }
        return first.getWorld().getUID().equals(second.getWorld().getUID())
                && first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ();
    }

    private String findRawKeyByNormalizedId(ConfigurationSection section, String normalizedId) {
        for (String key : section.getKeys(false)) {
            if (normalizedId.equals(normalizeId(key))) {
                return key;
            }
        }
        return null;
    }
}

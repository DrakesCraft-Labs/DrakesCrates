package me.jackstar.drakescrates.oracle;

import me.jackstar.drakescraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;

public final class OracleYamlRepository {
    private final JavaPlugin plugin; private final Map<String, Relicary> relicaries = new LinkedHashMap<>();
    public OracleYamlRepository(JavaPlugin plugin) { this.plugin = plugin; if (!new File(plugin.getDataFolder(), "oracle.yml").exists()) plugin.saveResource("oracle.yml", false); reload(); }
    public void reload() { relicaries.clear(); YamlConfiguration y = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "oracle.yml")); ConfigurationSection all = y.getConfigurationSection("relicaries"); if (all == null) return;
        for (String id : all.getKeys(false)) { ConfigurationSection s = all.getConfigurationSection(id); if (s == null) continue; List<OracleReward> rewards = new ArrayList<>(); ConfigurationSection rs = s.getConfigurationSection("rewards"); if (rs != null) for (String rid : rs.getKeys(false)) { ConfigurationSection r = rs.getConfigurationSection(rid); if (r == null) continue; rewards.add(new OracleReward(rid, r.getString("name", rid), r.getString("rarity", "COMUN"), Math.max(.01, r.getDouble("weight", 1)), material(r.getString("material"), Material.CHEST), r.getStringList("commands"))); }
            if (!rewards.isEmpty()) relicaries.put(id.toLowerCase(Locale.ROOT), new Relicary(id.toLowerCase(Locale.ROOT), s.getString("name", id), s.getString("key-name", "Ofrenda"), material(s.getString("material"), Material.ENDER_CHEST), List.copyOf(rewards))); }
    }
    private Material material(String value, Material fallback) { try { return Material.valueOf(value == null ? fallback.name() : value.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { return fallback; } }
    /** oracle.yml completo (para secciones como key-sources). */
    public YamlConfiguration raw() { return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "oracle.yml")); }
    public Collection<Relicary> all() { return Collections.unmodifiableCollection(relicaries.values()); }
    public Relicary get(String id) { return relicaries.get(id.toLowerCase(Locale.ROOT)); }
}

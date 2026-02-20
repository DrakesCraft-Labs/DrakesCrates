package me.jackstar.drakescrates.infrastructure.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CratesSettings {

    private final JavaPlugin plugin;
    private final File file;
    private int rouletteSteps = 50;
    private long rouletteTickSpeed = 2L;

    public CratesSettings(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crates-settings.yml");
        saveDefault();
        reload();
    }

    public void reload() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        rouletteSteps = Math.max(10, config.getInt("roulette.total-steps", 50));
        rouletteTickSpeed = Math.max(1L, config.getLong("roulette.tick-speed", 2L));
    }

    public int getRouletteSteps() {
        return rouletteSteps;
    }

    public long getRouletteTickSpeed() {
        return rouletteTickSpeed;
    }

    private void saveDefault() {
        if (plugin.getResource("crates-settings.yml") != null) {
            plugin.saveResource("crates-settings.yml", false);
            return;
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException ignored) {
        }
    }
}

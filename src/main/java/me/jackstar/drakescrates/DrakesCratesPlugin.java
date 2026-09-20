package me.jackstar.drakescrates;

import me.jackstar.drakescrates.application.repositories.CrateRepository;
import me.jackstar.drakescrates.application.usecases.OpenCrateUseCase;
import me.jackstar.drakescrates.infrastructure.config.CratesSettings;
import me.jackstar.drakescrates.infrastructure.persistence.yaml.YamlCrateRepository;
import me.jackstar.drakescrates.integration.papi.DrakesCratesPlaceholderExpansion;
import me.jackstar.drakescrates.presentation.animation.RouletteAnimation;
import me.jackstar.drakescrates.presentation.commands.DrakesCratesCommand;
import me.jackstar.drakescrates.presentation.editor.CrateEditorManager;
import me.jackstar.drakescrates.presentation.editor.CratePreviewManager;
import me.jackstar.drakescrates.presentation.listeners.CrateListener;
import me.jackstar.drakescrates.oracle.OracleGuiService;
import me.jackstar.drakescrates.oracle.OracleRepository;
import me.jackstar.drakescrates.oracle.OracleService;
import me.jackstar.drakescrates.oracle.OracleYamlRepository;
import me.jackstar.drakescrates.oracle.OraculoCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DrakesCratesPlugin extends JavaPlugin {

    private static final String[] DRAGON_BANNER = {
            "              / \\  //\\",
            "      |\\___/|      /   \\//  \\\\",
            "      /O  O  \\__  /    //  | \\ \\",
            "     /     /  \\/_/    //   |  \\  \\",
            "     \\_^_\\'/   \\/_   //    |   \\   \\"
    };

    private CrateRepository crateRepository;
    private RouletteAnimation rouletteAnimation;
    private CrateEditorManager crateEditorManager;
    private CrateListener crateListener;
    private CratesSettings cratesSettings;
    private OracleRepository oracleRepository;
    private OracleYamlRepository oracleYamlRepository;

    @Override
    public void onEnable() {
        logDragonBanner("DrakesCrates");
        logLoading("Saving default resources");
        saveDefaultResources();

        logLoading("Loading crate repository");
        crateRepository = new YamlCrateRepository(this);
        logLoading("Preparing use cases and animation");
        OpenCrateUseCase openCrateUseCase = new OpenCrateUseCase();
        cratesSettings = new CratesSettings(this);
        rouletteAnimation = new RouletteAnimation(this, cratesSettings.getRouletteSteps(), cratesSettings.getRouletteTickSpeed());
        crateEditorManager = new CrateEditorManager(crateRepository);
        CratePreviewManager cratePreviewManager = new CratePreviewManager();
        logLoading("Preparing El Oraculo");
        oracleYamlRepository = new OracleYamlRepository(this);
        oracleRepository = new OracleRepository(new File(getDataFolder(), "oracle.db"));
        OracleService oracleService = new OracleService(oracleRepository, oracleYamlRepository);
        OracleGuiService oracleGui = new OracleGuiService(this, oracleService, oracleYamlRepository);

        logLoading("Registering command executors");
        PluginCommand drakesCratesCommand = getCommand("drakescrates");
        if (drakesCratesCommand != null) {
            drakesCratesCommand.setExecutor(new DrakesCratesCommand(crateRepository, crateEditorManager, this::reloadRuntime));
        } else {
            getLogger().warning("Command 'drakescrates' not found in plugin.yml.");
        }
        PluginCommand oracleCommand = getCommand("oraculo");
        if (oracleCommand != null) oracleCommand.setExecutor(new OraculoCommand(oracleService, oracleYamlRepository, oracleGui, this::reloadRuntime));

        logLoading("Registering listeners");
        crateListener = new CrateListener(crateRepository, openCrateUseCase, rouletteAnimation, cratePreviewManager);
        getServer().getPluginManager().registerEvents(crateListener, this);
        getServer().getPluginManager().registerEvents(crateEditorManager, this);
        getServer().getPluginManager().registerEvents(cratePreviewManager, this);
        getServer().getPluginManager().registerEvents(oracleGui, this);
        // fuentes de llaves del Oraculo: votos (Votifier) y jefes; ver key-sources en oracle.yml
        new me.jackstar.drakescrates.oracle.KeySourcesListener(this, oracleService, () -> oracleYamlRepository.raw()).register();

        logLoading("Registering PlaceholderAPI expansion if available");
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DrakesCratesPlaceholderExpansion(crateRepository, oracleRepository).register();
        }

        getLogger().info("[Ready] DrakesCrates enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[Shutdown] DrakesCrates stopping...");
        if (rouletteAnimation != null) {
            rouletteAnimation.shutdown();
        }
        getLogger().info("[Shutdown] DrakesCrates disabled.");
    }

    public void reloadRuntime() {
        crateRepository.reload();
        cratesSettings.reload();
        if (oracleYamlRepository != null) oracleYamlRepository.reload();

        if (rouletteAnimation != null) {
            rouletteAnimation.shutdown();
        }
        rouletteAnimation = new RouletteAnimation(this, cratesSettings.getRouletteSteps(), cratesSettings.getRouletteTickSpeed());
        if (crateListener != null) {
            crateListener.setCrateAnimation(rouletteAnimation);
        }
        getLogger().info("[Reload] DrakesCrates runtime reloaded.");
    }

    private void saveDefaultResources() {
        File cratesFile = new File(getDataFolder(), "crates.yml");
        if (!cratesFile.exists() && getResource("crates.yml") != null) {
            saveResource("crates.yml", false);
        }
        File settingsFile = new File(getDataFolder(), "crates-settings.yml");
        if (!settingsFile.exists() && getResource("crates-settings.yml") != null) {
            saveResource("crates-settings.yml", false);
        }
    }

    private void logLoading(String step) {
        getLogger().info("[Loading] " + step + "...");
    }

    private void logDragonBanner(String pluginName) {
        getLogger().info("========================================");
        getLogger().info(" " + pluginName + " - loading");
        for (String line : DRAGON_BANNER) {
            getLogger().info(line);
        }
        getLogger().info("========================================");
    }
}

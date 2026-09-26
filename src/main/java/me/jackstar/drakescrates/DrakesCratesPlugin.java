package me.jackstar.drakescrates;

import me.jackstar.drakescrates.application.repositories.CrateRepository;
import me.jackstar.drakescrates.application.repositories.VirtualKeyRepository;
import me.jackstar.drakescrates.application.usecases.OpenCrateUseCase;
import me.jackstar.drakescrates.compat.SlimefunHook;
import me.jackstar.drakescrates.domain.modality.ModalityManager;
import me.jackstar.drakescrates.infrastructure.config.CratesSettings;
import me.jackstar.drakescrates.infrastructure.persistence.sqlite.SqliteVirtualKeyRepository;
import me.jackstar.drakescrates.infrastructure.persistence.yaml.YamlCrateRepository;
import me.jackstar.drakescrates.integration.papi.DrakesCratesPlaceholderExpansion;
import me.jackstar.drakescrates.oracle.OracleGuiService;
import me.jackstar.drakescrates.oracle.OracleRepository;
import me.jackstar.drakescrates.oracle.OracleService;
import me.jackstar.drakescrates.oracle.OracleYamlRepository;
import me.jackstar.drakescrates.oracle.OraculoCommand;
import me.jackstar.drakescrates.presentation.animation.RouletteAnimation;
import me.jackstar.drakescrates.presentation.commands.CratesPlayerCommand;
import me.jackstar.drakescrates.presentation.commands.DrakesCratesCommand;
import me.jackstar.drakescrates.presentation.editor.CrateEditorManager;
import me.jackstar.drakescrates.presentation.editor.CratePreviewManager;
import me.jackstar.drakescrates.presentation.gui.VirtualCrateMenu;
import me.jackstar.drakescrates.presentation.listeners.CrateListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DrakesCratesPlugin extends JavaPlugin {

    private CrateRepository crateRepository;
    private VirtualKeyRepository virtualKeyRepository;
    private ModalityManager modalityManager;
    private RouletteAnimation rouletteAnimation;
    private CrateEditorManager crateEditorManager;
    private CratePreviewManager cratePreviewManager;
    private VirtualCrateMenu virtualCrateMenu;
    private CrateListener crateListener;
    private CratesSettings cratesSettings;
    private OracleRepository oracleRepository;
    private OracleYamlRepository oracleYamlRepository;

    @Override
    public void onEnable() {
        getLogger().info("========================================");
        getLogger().info(" DrakesCrates 2.0 - Virtual & Modality Guard");
        getLogger().info("========================================");

        logLoading("Initializing Slimefun compatibility hook");
        SlimefunHook.init();

        logLoading("Saving default resources");
        saveDefaultResources();

        logLoading("Initializing Modality Manager & Guard");
        modalityManager = new ModalityManager(this);

        logLoading("Loading crate repository and virtual key SQLite storage");
        crateRepository = new YamlCrateRepository(this);
        virtualKeyRepository = new SqliteVirtualKeyRepository(new File(getDataFolder(), "virtual_keys.db"));

        logLoading("Preparing use cases and animation");
        OpenCrateUseCase openCrateUseCase = new OpenCrateUseCase(modalityManager);
        cratesSettings = new CratesSettings(this);
        rouletteAnimation = new RouletteAnimation(this, cratesSettings.getRouletteSteps(), cratesSettings.getRouletteTickSpeed());
        crateEditorManager = new CrateEditorManager(crateRepository);
        cratePreviewManager = new CratePreviewManager();

        logLoading("Preparing Virtual Crates GUI");
        virtualCrateMenu = new VirtualCrateMenu(this, crateRepository, virtualKeyRepository, modalityManager,
                openCrateUseCase, rouletteAnimation, cratePreviewManager);

        logLoading("Preparing El Oraculo");
        oracleYamlRepository = new OracleYamlRepository(this);
        oracleRepository = new OracleRepository(new File(getDataFolder(), "oracle.db"));
        OracleService oracleService = new OracleService(oracleRepository, oracleYamlRepository);
        OracleGuiService oracleGui = new OracleGuiService(this, oracleService, oracleYamlRepository);

        logLoading("Registering command executors");
        PluginCommand drakesCratesCommand = getCommand("drakescrates");
        if (drakesCratesCommand != null) {
            drakesCratesCommand.setExecutor(new DrakesCratesCommand(crateRepository, virtualKeyRepository,
                    crateEditorManager, virtualCrateMenu, this::reloadRuntime));
        }

        PluginCommand cratesCommand = getCommand("crates");
        if (cratesCommand != null) {
            cratesCommand.setExecutor(new CratesPlayerCommand(virtualCrateMenu, virtualKeyRepository));
        }

        PluginCommand oracleCommand = getCommand("oraculo");
        if (oracleCommand != null) {
            oracleCommand.setExecutor(new OraculoCommand(oracleService, oracleYamlRepository, oracleGui, this::reloadRuntime));
        }

        logLoading("Registering listeners");
        crateListener = new CrateListener(crateRepository, virtualKeyRepository, modalityManager,
                openCrateUseCase, rouletteAnimation, cratePreviewManager);
        getServer().getPluginManager().registerEvents(crateListener, this);
        getServer().getPluginManager().registerEvents(crateEditorManager, this);
        getServer().getPluginManager().registerEvents(cratePreviewManager, this);
        getServer().getPluginManager().registerEvents(virtualCrateMenu, this);
        getServer().getPluginManager().registerEvents(oracleGui, this);

        // Oraculo key sources
        new me.jackstar.drakescrates.oracle.KeySourcesListener(this, oracleService, () -> oracleYamlRepository.raw()).register();

        logLoading("Registering PlaceholderAPI expansion if available");
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DrakesCratesPlaceholderExpansion(crateRepository, virtualKeyRepository, modalityManager, oracleRepository).register();
        }

        getLogger().info("[Ready] DrakesCrates 2.0 fully enabled with Modality Guard & Virtual Crates.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[Shutdown] DrakesCrates stopping...");
        if (rouletteAnimation != null) {
            rouletteAnimation.shutdown();
        }
        if (virtualKeyRepository != null) {
            virtualKeyRepository.close();
        }
        getLogger().info("[Shutdown] DrakesCrates disabled.");
    }

    public void reloadRuntime() {
        if (modalityManager != null) modalityManager.reload();
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
}

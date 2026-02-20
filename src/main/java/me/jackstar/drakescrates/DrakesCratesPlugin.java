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
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DrakesCratesPlugin extends JavaPlugin {

    private CrateRepository crateRepository;
    private RouletteAnimation rouletteAnimation;
    private CrateEditorManager crateEditorManager;
    private CratesSettings cratesSettings;

    @Override
    public void onEnable() {
        saveDefaultResources();

        crateRepository = new YamlCrateRepository(this);
        OpenCrateUseCase openCrateUseCase = new OpenCrateUseCase();
        cratesSettings = new CratesSettings(this);
        rouletteAnimation = new RouletteAnimation(this, cratesSettings.getRouletteSteps(), cratesSettings.getRouletteTickSpeed());
        crateEditorManager = new CrateEditorManager(crateRepository);
        CratePreviewManager cratePreviewManager = new CratePreviewManager();

        PluginCommand drakesCratesCommand = getCommand("drakescrates");
        if (drakesCratesCommand != null) {
            drakesCratesCommand.setExecutor(new DrakesCratesCommand(crateRepository, crateEditorManager));
        } else {
            getLogger().warning("Command 'drakescrates' not found in plugin.yml.");
        }

        getServer().getPluginManager().registerEvents(
                new CrateListener(crateRepository, openCrateUseCase, rouletteAnimation, cratePreviewManager),
                this);
        getServer().getPluginManager().registerEvents(crateEditorManager, this);
        getServer().getPluginManager().registerEvents(cratePreviewManager, this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DrakesCratesPlaceholderExpansion(crateRepository).register();
        }
    }

    @Override
    public void onDisable() {
        if (rouletteAnimation != null) {
            rouletteAnimation.shutdown();
        }
    }

    private void saveDefaultResources() {
        if (getResource("crates.yml") != null) {
            saveResource("crates.yml", false);
        }
        if (getResource("crates-settings.yml") != null) {
            saveResource("crates-settings.yml", false);
        }
    }
}

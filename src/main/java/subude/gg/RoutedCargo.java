package subude.gg;

import org.bukkit.plugin.java.JavaPlugin;

public final class RoutedCargo extends JavaPlugin {
    ConfigManager configManager;
    SpawnManager spawnManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        spawnManager = new SpawnManager(configManager);

        getCommand("cargo").setExecutor(new CargoCommands(spawnManager, configManager));
        getLogger().info("RoutedCargo Load");
    }

    @Override
    public void onDisable() {

    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

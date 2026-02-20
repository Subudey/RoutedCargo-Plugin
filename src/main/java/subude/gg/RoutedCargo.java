package subude.gg;

import org.bukkit.plugin.java.JavaPlugin;

public final class RoutedCargo extends JavaPlugin {
    ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        getLogger().info("RoutedCargo Load");
    }

    @Override
    public void onDisable() {

    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

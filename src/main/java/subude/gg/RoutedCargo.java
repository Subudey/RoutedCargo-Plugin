package subude.gg;

import org.bukkit.plugin.java.JavaPlugin;

public final class RoutedCargo extends JavaPlugin {
    ConfigManager configManager = new ConfigManager(this);

    @Override
    public void onEnable() {
        getLogger().info("RoutedCargo Load");
    }

    @Override
    public void onDisable() {

    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

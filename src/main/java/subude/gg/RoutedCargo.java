package subude.gg;

import org.bukkit.plugin.java.JavaPlugin;
import subude.gg.Managers.ConfigManager;
import subude.gg.Managers.EventManager;
import subude.gg.Managers.LootManager;
import subude.gg.Managers.SpawnManager;

public final class RoutedCargo extends JavaPlugin {
    private ConfigManager configManager;
    private SpawnManager spawnManager;
    private EventManager eventManager;
    private LootManager lootManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        lootManager = new LootManager(configManager);
        spawnManager = new SpawnManager(configManager, lootManager,this);
        eventManager = new EventManager(configManager,spawnManager);

        getCommand("cargo").setExecutor(new CargoCommands(spawnManager, configManager, eventManager));
        getLogger().info("RoutedCargo Load");
    }

    @Override
    public void onDisable() {
        spawnManager.removeStructure();
        getLogger().info("RoutedCargo Unload");
    }
}

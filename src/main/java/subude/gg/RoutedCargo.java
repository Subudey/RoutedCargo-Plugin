package subude.gg;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import subude.gg.Managers.*;

public final class RoutedCargo extends JavaPlugin {
    private ConfigManager configManager;
    private SpawnManager spawnManager;
    private EventManager eventManager;
    private LootManager lootManager;
    private RandomStageEffectsManager randomStageEffectsManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        lootManager = new LootManager(configManager);
        spawnManager = new SpawnManager(configManager, lootManager,this);
        randomStageEffectsManager = new RandomStageEffectsManager(spawnManager, lootManager, this);
        eventManager = new EventManager(configManager,spawnManager, randomStageEffectsManager);

        getCommand("cargo").setExecutor(new CargoCommands(spawnManager, configManager, eventManager));
        Bukkit.getPluginManager().registerEvents(randomStageEffectsManager, this);
        getLogger().info("RoutedCargo Load");
    }

    @Override
    public void onDisable() {
        spawnManager.removeStructure();
        getLogger().info("RoutedCargo Unload");
    }
}

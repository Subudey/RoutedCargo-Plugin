package subude.gg;

import org.bukkit.Bukkit;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;
import subude.gg.Managers.*;

public final class RoutedCargo extends JavaPlugin {
    private ConfigManager configManager;
    private SpawnManager spawnManager;
    private EventManager eventManager;
    private LootManager lootManager;
    private CargoListener cargoListener;
    private RandomStageEffectsManager randomStageEffectsManager;
    private CargoController cargoController;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        lootManager = new LootManager(configManager);
        spawnManager = new SpawnManager(configManager, lootManager,this);
        randomStageEffectsManager = new RandomStageEffectsManager(spawnManager, lootManager, this);
        eventManager = new EventManager(configManager,spawnManager, randomStageEffectsManager, this);
        cargoController = new CargoController(this,spawnManager,eventManager,configManager, lootManager);
        cargoListener = new CargoListener(this, randomStageEffectsManager.getActiveMeteors());

        getCommand("cargo").setExecutor(new CargoCommands(cargoController,configManager, spawnManager));
        Bukkit.getPluginManager().registerEvents(cargoListener, this);
        getLogger().info("RoutedCargo Load");
        cargoController.startCycle();
    }

    @Override
    public void onDisable() {
        if (cargoController != null) {
            cargoController.stopImmediately(); // метод который отменяет таймер
        }

        if (spawnManager != null) {
            spawnManager.removeStructure();
        }

        for (FallingBlock meteor : randomStageEffectsManager.getActiveMeteors()) {
            if (meteor != null && meteor.isValid()) {
                meteor.remove();
            }
        }
        randomStageEffectsManager.getActiveMeteors().clear();

        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("RoutedCargo Unload");
    }
}

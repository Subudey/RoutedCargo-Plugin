package subude.gg;

import org.bukkit.plugin.java.JavaPlugin;
import subude.gg.Managers.*;

public class CargoContext {
    public final JavaPlugin plugin;
    public ConfigManager configManager;
    public LootManager lootManager;
    public SpawnManager spawnManager;
    public BossBarManager bossBarManager;
    public RandomStageEffectsManager randomStageEffectsManager;
    public EventManager eventManager;
    public CargoController cargoController;
    public CargoListener cargoListener;

    public CargoContext(JavaPlugin plugin) {
        this.plugin = plugin;
    }
}

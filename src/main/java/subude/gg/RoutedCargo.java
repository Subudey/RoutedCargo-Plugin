package subude.gg;

import org.bukkit.Bukkit;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;
import subude.gg.Managers.*;

public final class RoutedCargo extends JavaPlugin {
    private CargoContext ctx;

    @Override
    public void onEnable() {
        ctx = new CargoContext(this);

        ctx.configManager = new ConfigManager(this);
        ctx.lootManager = new LootManager(ctx.configManager);
        ctx.spawnManager = new SpawnManager(ctx);
        ctx.bossBarManager = new BossBarManager(ctx);
        ctx.randomStageEffectsManager = new RandomStageEffectsManager(ctx);
        ctx.eventManager = new EventManager(ctx);
        ctx.cargoController = new CargoController(ctx);
        ctx.cargoListener = new CargoListener(ctx.randomStageEffectsManager.getActiveMeteors());

        getCommand("cargo").setExecutor(new CargoCommands(ctx));
        Bukkit.getPluginManager().registerEvents(ctx.cargoListener, this);
        getLogger().info("RoutedCargo Load");
        ctx.cargoController.startCycle();
    }

    @Override
    public void onDisable() {
        if (ctx.cargoController != null) {
            ctx.cargoController.stopImmediately();
        }

        if (ctx.spawnManager != null) {
            ctx.spawnManager.removeStructure();
        }

        if (ctx.randomStageEffectsManager != null) {
            for (FallingBlock meteor : ctx.randomStageEffectsManager.getActiveMeteors()) {
                if (meteor != null && meteor.isValid()) {
                    meteor.remove();
                }
            }
            ctx.randomStageEffectsManager.getActiveMeteors().clear();
        }

        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("RoutedCargo Unload");
    }
}

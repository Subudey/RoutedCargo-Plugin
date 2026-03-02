package subude.gg;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import subude.gg.Managers.ConfigManager;
import subude.gg.Managers.EventManager;
import subude.gg.Managers.LootManager;
import subude.gg.Managers.SpawnManager;

import java.util.List;

public class CargoController {

    private final JavaPlugin plugin;
    private final SpawnManager spawnManager;
    private final EventManager eventManager;
    private final ConfigManager configManager;
    private final LootManager lootManager;
    private Location preparedLocation;
    private BukkitTask currentTask;
    private CargoState state = CargoState.WAITING_FOR_EVENT;

    public enum CargoState {
        WAITING_FOR_EVENT,
        PREPARING,
        RUNNING
    }

    public CargoController(JavaPlugin plugin, SpawnManager spawnManager, EventManager eventManager, ConfigManager configManager, LootManager lootManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
        this.eventManager = eventManager;
        this.configManager = configManager;
        this.lootManager = lootManager;
    }

    public void startCycle() {
        if (state == CargoState.RUNNING) return;

        state = CargoState.WAITING_FOR_EVENT;
        scheduleEventStart();
    }

    private void scheduleEventStart() {
        cancelTask();

        currentTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            announceEvent();
            schedulePreparation();
        }, configManager.eventInterval * 20L * 60L);
    }

    private void announceEvent() {
        state = CargoState.PREPARING;
        preparedLocation = spawnManager.findSafeLocation();
        lootManager.selectCargoType();
        spawnManager.setPreparedLocation(preparedLocation);

        if (preparedLocation == null) {
            Bukkit.broadcastMessage("§cНе удалось найти место для события!");
            startCycle();
        }

        configManager.startMessage.stream().forEach(message -> Bukkit.broadcastMessage(spawnManager.applyPlaceholders(message)));
        playGlobalSound(configManager.spawnSounds, 8F, 1F);
    }

    private void schedulePreparation() {
        cancelTask();

        currentTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            spawnEvent();
            scheduleNextStage();
        }, configManager.eventStartInterval * 20L);
    }

    private void spawnEvent() {
        state = CargoState.RUNNING;
        spawnManager.spawnStructure(preparedLocation);
    }

    private void scheduleNextStage() {
        cancelTask();

        currentTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (spawnManager.getCurrentStep() >= 4) {
                eventManager.finishEvent();
                spawnManager.removeStructure();

                state = CargoState.WAITING_FOR_EVENT;
                startCycle();
                return;
            }

            eventManager.nextCargoStage();
            scheduleNextStage();

        }, configManager.eventDuration * 20L);
    }

    public void forceStart() {
        cancelTask();

        if (state == CargoState.RUNNING || state == CargoState.PREPARING) {
            return;
        }

        announceEvent();
        schedulePreparation();
    }

    public void forceStop() {
        cancelTask();

        eventManager.finishEvent();
        spawnManager.removeStructure();
        state = CargoState.WAITING_FOR_EVENT;

        startCycle();
    }

    public boolean isRunning() {
        return state == CargoState.RUNNING || state == CargoState.PREPARING;
    }

    public CargoState getState() {
        return state;
    }

    public void stopImmediately() {
        cancelTask();
    }

    private void cancelTask() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    public void playGlobalSound(List<Sound> sounds, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sounds.stream().forEach(sound -> player.playSound(player.getLocation(),sound,volume,pitch));
        }
    }
}
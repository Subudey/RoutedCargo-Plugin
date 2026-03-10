package subude.gg;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import subude.gg.Managers.*;

import java.util.List;

public class CargoController {
    private CargoContext cxt;
    private Location preparedLocation;
    private BukkitTask currentTask;
    private BukkitTask particleTask;
    private CargoState state = CargoState.WAITING_FOR_EVENT;
    private long startTime;
    private double beamRotation = 0;

    public enum CargoState {
        WAITING_FOR_EVENT,
        PREPARING,
        RUNNING
    }

    public CargoController(CargoContext cxt) {
        this.cxt = cxt;
    }

    public void startCycle() {
        if (state == CargoState.RUNNING) return;

        state = CargoState.WAITING_FOR_EVENT;
        scheduleEventStart();
    }

    private void scheduleEventStart() {
        cancelTask();

        currentTask = Bukkit.getScheduler().runTaskLater(cxt.plugin, () -> {
            announceEvent();
            schedulePreparation();
        }, cxt.configManager.eventInterval * 20L * 60L);
    }

    private void announceEvent() {
        state = CargoState.PREPARING;

        startTime = System.currentTimeMillis() + (cxt.configManager.eventStartInterval * 1000L);
        preparedLocation = cxt.spawnManager.findSafeLocation();
        cxt.lootManager.selectCargoType();
        cxt.spawnManager.setPreparedLocation(preparedLocation);
        startPreparationBeam();

        if (preparedLocation == null) {
            Bukkit.broadcastMessage("§cНе удалось найти место для события!");
            startCycle();
        }

        cxt.configManager.startMessage.stream().forEach(message -> Bukkit.broadcastMessage(cxt.spawnManager.applyPlaceholders(message)));
        playGlobalSound(cxt.configManager.spawnSounds, 8F, 1F);
        cxt.bossBarManager.startPreparing(cxt.configManager.eventStartInterval);
    }

    private void schedulePreparation() {
        cancelTask();

        currentTask = Bukkit.getScheduler().runTaskLater(cxt.plugin, () -> {
            spawnEvent();
            scheduleNextStage();
        }, cxt.configManager.eventStartInterval * 20L);
    }

    private void spawnEvent() {
        state = CargoState.RUNNING;

        stopPreparationBeam();
        cxt.spawnManager.spawnStructure(preparedLocation);
        cxt.bossBarManager.startCargo(cxt.configManager.eventDuration, 1);
    }

    private void scheduleNextStage() {
        cancelTask();

        currentTask = Bukkit.getScheduler().runTaskLater(cxt.plugin, () -> {
            if (cxt.spawnManager.getCurrentStep() >= 4) {
                cxt.eventManager.finishEvent();
                cxt.spawnManager.removeStructure();

                state = CargoState.WAITING_FOR_EVENT;
                cxt.bossBarManager.stop();
                startCycle();
                return;
            }

            cxt.eventManager.nextCargoStage();
            cxt.bossBarManager.updateRail(cxt.spawnManager.getCurrentStep(), cxt.configManager.eventDuration);
            scheduleNextStage();

        }, cxt.configManager.eventDuration * 20L);
    }

    private void startPreparationBeam() {
        if (preparedLocation == null) return;

        particleTask = Bukkit.getScheduler().runTaskTimer(cxt.plugin, () -> {
            if (state != CargoState.PREPARING) {
                stopPreparationBeam();
                return;
            }

            World world = preparedLocation.getWorld();
            Location center = preparedLocation.clone();

            int maxHeight = world.getMaxHeight();
            double radius = 1.5;

            beamRotation += 4;
            if (beamRotation >= 360) beamRotation = 0;

            for (double y = 0; y <= maxHeight - center.getY(); y += 0.5) {
                double currentY = center.getY() + y;

                for (int angle = 0; angle < 360; angle += 20) {
                    double radians = Math.toRadians(angle + beamRotation);
                    double x = Math.cos(radians) * radius;
                    double z = Math.sin(radians) * radius;

                    Location particleLoc = new Location(world, center.getX() + x, currentY, center.getZ() + z);

                    for (Player player : world.getPlayers()) {
                        if (player.getLocation().distance(center) >= 90) continue;
                        player.spawnParticle(Particle.REDSTONE, particleLoc,1,0,0,0,0, new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.8F));
                    }
                }
            }
        }, 0L, 30L);
    }

    private void stopPreparationBeam() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
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

        cxt.eventManager.finishEvent();
        stopPreparationBeam();
        cxt.spawnManager.removeStructure();
        state = CargoState.WAITING_FOR_EVENT;
        cxt.bossBarManager.stop();

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

    public int getTimeLeft() {
        long diff = startTime - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return (int) (diff / 1000);
    }
}
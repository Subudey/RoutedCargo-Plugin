package subude.gg.Managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import subude.gg.CargoContext;

public class BossBarManager {
    private CargoContext cxt;
    private BossBar bossBar;
    private BukkitTask timerTask;
    private int timeLeft;
    private int maxTime;
    private int currentRail = 0;
    private String stage = "";

    public BossBarManager(CargoContext cxt) {
        this.cxt = cxt;
    }

    public void createBar() {
        if (bossBar != null) return;

        bossBar = Bukkit.createBossBar("Cargo Event", BarColor.BLUE, BarStyle.SEGMENTED_10);
        bossBar.setVisible(true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }

    public void startPreparing(int duration) {
        createBar();

        this.stage = "Preparing";
        this.currentRail = 0;
        startTimer(duration);
    }

    public void startCargo(int duration, int rail) {
        createBar();

        this.stage = "Running";
        this.currentRail = rail;
        startTimer(duration);
    }

    public void updateRail(int rail, int duration) {
        this.currentRail = rail;
        startTimer(duration);
    }

    private void startTimer(int duration) {
        stopTimer();

        this.timeLeft = duration;
        this.maxTime = duration;

        timerTask = Bukkit.getScheduler().runTaskTimer(cxt.plugin, () -> {
            if (timeLeft <= 0) {
                stopTimer();
                return;
            }

            updateBar();
            timeLeft--;

        }, 0L, 20L);
    }

    private void updateBar() {
        if (bossBar == null) return;
        String title;

        if (currentRail > 0) {
            title = "§6Маршруточный груз §7| §eСтадия " + currentRail + "/" + 4 + " §7| §f" + timeLeft + "сек";
        } else {
            title = "§6Маршруточный груз §7| §e" + "До начала " + " §7: §f" + timeLeft + "сек";
        }

        bossBar.setTitle(title);

        double progress = (double) timeLeft / maxTime;
        progress = Math.max(0, Math.min(1, progress));

        bossBar.setProgress(progress);
    }

    public void addPlayer(Player player) {
        if (bossBar != null) {
            bossBar.addPlayer(player);
        }
    }

    public void removePlayer(Player player) {
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }

    public void stop() {
        stopTimer();

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    private void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

}

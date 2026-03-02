package subude.gg;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import subude.gg.Managers.ConfigManager;
import subude.gg.Managers.SpawnManager;

public class CargoCommands implements CommandExecutor {
    private final CargoController cargoController;
    private final ConfigManager configManager;
    private final SpawnManager spawnManager;

    public CargoCommands(CargoController cargoController, ConfigManager configManager, SpawnManager spawnManager) {
        this.cargoController = cargoController;
        this.configManager = configManager;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eИспользование /cargo <start|stop|status|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (cargoController.isRunning()) {
                    sender.sendMessage("§cИвент уже идёт!");
                    return true;
                }

                cargoController.forceStart();
                sender.sendMessage("§aИвент запущен принудительно.");
            }

            case "stop" -> {
                if (cargoController.getState() == CargoController.CargoState.WAITING_FOR_EVENT) {
                    sender.sendMessage("§cИвент не идёт!");
                    return true;
                }

                cargoController.forceStop();
                sender.sendMessage("§cИвент остановлен.");
            }

            case "status" -> {
                if (cargoController.isRunning()) {
                    configManager.statusGoMessage.forEach(message -> sender.sendMessage(spawnManager.applyPlaceholders(message)));
                } else {
                    configManager.statusNoneMessage.forEach(message -> sender.sendMessage(spawnManager.applyPlaceholders(message)));
                }
            }

            case "reload" -> {
                configManager.reload();
                sender.sendMessage("§aКонфиг успешно перезагружен!");
            }

            default -> {
                sender.sendMessage("§cНекорректный аргумент");
                sender.sendMessage("§cИспользуете /cargo <start|stop|status|reload>");
            }

        }

        return true;
    }
}

package subude.gg;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import subude.gg.Managers.ConfigManager;
import subude.gg.Managers.SpawnManager;

public class CargoCommands implements CommandExecutor {
    private CargoContext cxt;

    public CargoCommands(CargoContext cxt) {
        this.cxt = cxt;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eИспользование /cargo <start|stop|status|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (cxt.cargoController.isRunning()) {
                    sender.sendMessage("§cИвент уже идёт!");
                    return true;
                }

                cxt.cargoController.forceStart();
                sender.sendMessage("§aИвент запущен принудительно.");
            }

            case "stop" -> {
                if (cxt.cargoController.getState() == CargoController.CargoState.WAITING_FOR_EVENT) {
                    sender.sendMessage("§cИвент не идёт!");
                    return true;
                }

                cxt.cargoController.forceStop();
                sender.sendMessage("§cИвент остановлен.");
            }

            case "status" -> {
                if (cxt.cargoController.getState() == CargoController.CargoState.RUNNING) {
                    cxt.configManager.statusGoMessage.forEach(message -> sender.sendMessage(cxt.spawnManager.applyPlaceholders(message)));
                } else if (cxt.cargoController.getState() == CargoController.CargoState.PREPARING) {
                    cxt.configManager.statusPrepareMessage.forEach(message -> sender.sendMessage(cxt.spawnManager.applyPlaceholders(message)));
                } else {
                    cxt.configManager.statusNoneMessage.forEach(message -> sender.sendMessage(cxt.spawnManager.applyPlaceholders(message)));
                }
            }

            case "reload" -> {
                cxt.configManager.reload();
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

package subude.gg;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import subude.gg.Managers.ConfigManager;
import subude.gg.Managers.EventManager;
import subude.gg.Managers.SpawnManager;

public class CargoCommands implements CommandExecutor {
    private final SpawnManager spawnManager;
    private final ConfigManager configManager;
    private final EventManager eventManager;

    public CargoCommands(SpawnManager spawnManager, ConfigManager configManager, EventManager eventManager) {
        this.spawnManager = spawnManager;
        this.configManager = configManager;
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eИспользование /cargo <start|stop|status|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (spawnManager.getMinecart() != null) {
                    sender.sendMessage("§cИвент уже идёт!");
                } else if (spawnManager.spawnStructure()) {
                    sender.sendMessage("§cВагонетка заспавнена!");
                } else {
                    sender.sendMessage("§cНе удалось найти место для спавна.");
                }
            }

            case "stop" -> {
                if (spawnManager.getMinecart() == null) {
                    sender.sendMessage("§cИвента не обнаруженно!");
                } else {
                    spawnManager.removeStructure();
                    sender.sendMessage("§cВагонетка удалена.");
                }
            }

            case "status" -> {
                if (spawnManager.getMinecart() != null) {
                    configManager.statusGoMessage.stream().forEach(x -> sender.sendMessage(spawnManager.applyPlaceholders(x)));
                } else {
                    configManager.statusNoneMessage.stream().forEach(x -> sender.sendMessage(spawnManager.applyPlaceholders(x)));
                }
            }

            case "nextstage" -> {
                if (spawnManager.getMinecart() != null) {
                    eventManager.nextCargoStage();
                } else {
                    sender.sendMessage("Ивента не обнаруженно");
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

package subude.gg;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CargoCommands implements CommandExecutor {
    private final SpawnManager spawnManager;
    private final ConfigManager configManager;

    public CargoCommands(SpawnManager spawnManager, ConfigManager configManager) {
        this.spawnManager = spawnManager;
        this.configManager = configManager;
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
                    for (String statusGoMessage: configManager.statusGoMessage) {
                        sender.sendMessage(spawnManager.applyPlaceholders(statusGoMessage));
                    }
                } else {
                    for (String statusNoneMessage: configManager.statusNoneMessage) {
                        sender.sendMessage(spawnManager.applyPlaceholders(statusNoneMessage));
                    }
                }
            }

            case "reload" -> {
                configManager.reload();
                sender.sendMessage("§aКонфиг испешно перезагружен!");
            }

            default -> {
                sender.sendMessage("§cНекорректный аргумент");
                sender.sendMessage("§cИспользуете /cargo <start|stop|status>");
            }

        }

        return true;
    }
}

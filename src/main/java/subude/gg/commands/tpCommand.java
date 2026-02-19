package subude.gg.commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.minecart.StorageMinecart;
import subude.gg.SpawnManager;

public class tpCommand implements CommandExecutor {
    private final SpawnManager spawnManager;

    public tpCommand(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }
    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("§cИспользование: /cargotp <x> <y> <z>");
            return true;
        }

        StorageMinecart minecart = spawnManager.getMinecart();

        if (minecart == null || minecart.isDead()) {
            sender.sendMessage("§cВагонетка не заспавнена!");
            return true;
        }

        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            World world = minecart.getWorld();
            Location target = new Location(world, x + 0.5, y, z + 0.5);
            minecart.teleport(target); sender.sendMessage("§aВагонетка телепортирована!");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cКоординаты должны быть числами!");
        }
        return true;
    }
}

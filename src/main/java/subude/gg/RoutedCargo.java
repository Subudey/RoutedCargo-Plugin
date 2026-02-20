package subude.gg;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class RoutedCargo extends JavaPlugin {
    private StorageMinecart minecart;

    @Override
    public void onEnable() {
        getLogger().info("TestCart enabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("cartspawn")) {

            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("Only players");
                return true;
            }
            Player player = (Player) sender;

            World world = player.getWorld();
            Location loc = player.getLocation().getBlock().getLocation();

            loc.getBlock().setType(Material.RAIL);

            Location spawnLoc = loc.clone().add(0.5, 0.1, 0.5);

            minecart = world.spawn(spawnLoc, StorageMinecart.class);
            minecart.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));

            player.sendMessage("§aВагонетка заспавнена");
            return true;
        }

        if (command.getName().equalsIgnoreCase("carttp")) {

            if (minecart == null || minecart.isDead()) {
                sender.sendMessage("§cВагонетки нет");
                return true;
            }

            if (args.length != 3) {
                sender.sendMessage("/carttp x y z");
                return true;
            }

            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);

            Location target = new Location(minecart.getWorld(), x, y, z);

            if (target.getBlock().getType() != Material.RAIL) {
                target.getBlock().setType(Material.RAIL);
            }

            minecart.teleport(target.clone().add(0.5, 0.1, 0.5));

            sender.sendMessage("§eТелепортирована");
            return true;
        }

        if (command.getName().equalsIgnoreCase("cartmove")) {

            if (minecart == null || minecart.isDead()) {
                sender.sendMessage("§cВагонетки нет");
                return true;
            }

            minecart.setVelocity(new Vector(0.4, 0, 0));
            sender.sendMessage("§bТолчок дан");
            return true;
        }

        return false;
    }
}

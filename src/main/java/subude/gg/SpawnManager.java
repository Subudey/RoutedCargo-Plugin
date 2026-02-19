package subude.gg;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.minecart.StorageMinecart;
public class SpawnManager {
    private StorageMinecart minecart;

    public void spawnCart(World world, double x, double y, double z) {
        Location railLocation = new Location(world,x,y,z); railLocation.getBlock().setType(Material.RAIL);
        Location cartLocation = railLocation.clone().add(0.5,0,0.5);
        minecart = world.spawn(cartLocation, StorageMinecart.class);
    }

    public StorageMinecart getMinecart() {
        return minecart;
    }
}
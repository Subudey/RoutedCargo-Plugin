package subude.gg;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.StorageMinecart;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnManager {
    private final ConfigManager configManager;
    private final Random random = new Random();

    private Location spawnLocation;
    private StorageMinecart minecart;
    private final List<Block> placedBlocks = new ArrayList<>();

    public SpawnManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean spawnStructure() {
        if (minecart != null && !minecart.isDead()) {
            removeStructure();
        }

        Location loc = findSafeLocation();
        if (loc == null) return false;

        this.spawnLocation = loc;

        buildRails(loc);
        spawnMinecart(loc);

        return true;
    }

    private Location findSafeLocation() {
        World world = Bukkit.getWorld("world");
        int radius = configManager.spawnRadius;
        int minY = configManager.minY;

        if (world == null) return null;

        for (int i = 0; i < configManager.attemps; i++) {

            int x = random.nextInt(radius * 2) - radius;
            int z = random.nextInt(radius * 2) - radius;

            int y = world.getHighestBlockYAt(x, z);

            if (y < minY) continue;

            Block ground = world.getBlockAt(x, y - 1, z);
            Material type = ground.getType();

            if (!type.isSolid() || type == Material.WATER || type == Material.LAVA) continue;

            boolean cliff = true;

            for (int i2 = 1; i2 <= 3; i2++) {
                Block front = world.getBlockAt(x, y - 1, z - i2);
                if (front.getType().isSolid()) {
                    cliff = false;
                    break;
                }
            }

            if (!cliff) continue;

            return new Location(world, x, y, z);
        }
        return null;
    }

    private void buildRails(Location center) {
        World world = center.getWorld();

        for (int i = 0; i < 5; i++) {

            Block block = world.getBlockAt(
                    center.getBlockX(),
                    center.getBlockY(),
                    center.getBlockZ() - i
            );

            block.setType(Material.RAIL);
            placedBlocks.add(block);
        }
    }

    private void spawnMinecart(Location loc) {
        minecart = (StorageMinecart) loc.getWorld().spawnEntity(
                loc.clone().add(0.5, 0, 0.5),
                EntityType.MINECART_CHEST
        );

        minecart.setCustomName("§6Маршруточный Груз");
        minecart.setCustomNameVisible(true);
        minecart.setGravity(false);
        minecart.setInvulnerable(true);
        minecart.setPersistent(true);
    }

    public void removeStructure() {

        if (minecart != null && !minecart.isDead()) {
            minecart.remove();
        }

        for (Block block : placedBlocks) {
            block.setType(Material.AIR);
        }

        placedBlocks.clear();
        spawnLocation = null;
        minecart = null;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public StorageMinecart getMinecart() {
        return minecart;
    }
}
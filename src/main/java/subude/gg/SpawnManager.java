package subude.gg;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SpawnManager {
    private final ConfigManager configManager;
    private final Random random = new Random();
    private Location spawnLocation;
    private UUID minecartUUID;
    private final List<Block> placedBlocks = new ArrayList<>();
    private final NamespacedKey key;

    public SpawnManager(ConfigManager configManager, JavaPlugin plugin) {
        this.configManager = configManager;
        this.key = new NamespacedKey(plugin,"cargo_minecart");
    }

    public boolean spawnStructure() {
        if (getMinecart() != null && !getMinecart().isDead()) {
            removeStructure();
        }

        Location loc = findSafeLocation();
        if (loc == null) return false;
        loc.getChunk().setForceLoaded(true);

        this.spawnLocation = loc;

        buildRails(loc);
        spawnMinecart(loc);

        for (String startMessage : configManager.startMessage) {
            Bukkit.broadcastMessage(applyPlaceholders(startMessage));
        }

        return true;
    }

    private Location findSafeLocation() {
        World world = Bukkit.getWorld("world");
        if (world == null) return null;

        int radius = configManager.spawnRadius;
        int minY = configManager.minY;

        for (int attempt = 0; attempt < configManager.attemps; attempt++) {
            int x = random.nextInt(radius * 2) - radius;
            int z = random.nextInt(radius * 2) - radius;

            int y = world.getHighestBlockYAt(x, z);
            if (y < minY) continue;

            boolean valid = true;
            for (int dz = 0; dz < 5; dz++) {
                Block ground = world.getBlockAt(x, y - 1, z - dz);
                Material type = ground.getType();
                if (!type.isSolid() || type == Material.WATER || type == Material.LAVA) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                Block cliff = world.getBlockAt(x, y - 1, z - 5);
                if (cliff.getType().isSolid()) valid = false;
            }

            if (valid) {
                return new Location(world, x + 0.5, y, z + 0.5);
            }
        }

        return null;
    }

    private void buildRails(Location start) {
        World world = start.getWorld();
        int x = start.getBlockX();
        int y = start.getBlockY();
        int z = start.getBlockZ();

        for (int i = 0; i < 5; i++) {
            Block railBlock = world.getBlockAt(x, y, z - i);
            railBlock.setType(Material.RAIL);
            placedBlocks.add(railBlock);
        }
    }

    private void spawnMinecart(Location loc) {
        StorageMinecart cart =
                (StorageMinecart) loc.getWorld().spawnEntity(loc, EntityType.MINECART_CHEST);

        minecartUUID = cart.getUniqueId();

        cart.setCustomName("§6Маршруточный Груз");
        cart.setGravity(false);
        cart.setInvulnerable(true);
        cart.setSilent(true);
        cart.setPersistent(true);
    }

    public void removeStructure() {
        StorageMinecart cart = getMinecart();

        if (cart != null && !cart.isDead()) {
            cart.remove();
        }

        minecartUUID = null;

        if (spawnLocation != null) {
            spawnLocation.getChunk().setForceLoaded(false);
        }

        for (Block block : placedBlocks) {
            block.setType(Material.AIR);
        }
        placedBlocks.clear();

        for (String endMessage : configManager.endMessage) {
            Bukkit.broadcastMessage(applyPlaceholders(endMessage));
        }
    }

    public String applyPlaceholders(String message) {
        if (spawnLocation != null) {
            message = message
                    .replace("%x%", String.valueOf(spawnLocation.getBlockX()))
                    .replace("%y%", String.valueOf(spawnLocation.getBlockY()))
                    .replace("%z%", String.valueOf(spawnLocation.getBlockZ()));
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public StorageMinecart getMinecart() {
        if (minecartUUID == null) return null;

        for (World world : Bukkit.getWorlds()) {
            Entity entity = world.getEntity(minecartUUID);

            if (entity instanceof StorageMinecart cart) {
                return cart;
            }
        }

        return null;
    }
}
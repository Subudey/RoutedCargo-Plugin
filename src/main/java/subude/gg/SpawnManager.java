package subude.gg;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class SpawnManager {
    private final ConfigManager configManager;
    private final Random random = new Random();
    private final List<Block> placedBlocks = new ArrayList<>();
    private final NamespacedKey key;
    private Location spawnLocation;
    private UUID minecartUUID;
    private BlockFace railDirection;

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

            Location base = new Location(world, x, y, z);
            for (BlockFace face : new BlockFace[]{
                    BlockFace.NORTH,
                    BlockFace.SOUTH,
                    BlockFace.EAST,
                    BlockFace.WEST
            }) {
                if (canPlaceRails(base, face)) {
                    railDirection = face;
                    return base.add(0.5, 1, 0.5);
                }
            }
        }
        return null;
    }

    private boolean canPlaceRails(Location start, BlockFace direction) {
        World world = start.getWorld();
        int x = start.getBlockX();
        int y = start.getBlockY();
        int z = start.getBlockZ();

        for (int i = 0; i < 5; i++) {

            int dx = direction.getModX() * i;
            int dz = direction.getModZ() * i;

            Block ground = world.getBlockAt(x + dx, y, z + dz);
            Block railSpace = world.getBlockAt(x + dx, y + 1, z + dz);
            Block headSpace = world.getBlockAt(x + dx, y + 2, z + dz);

            if (!ground.getType().isSolid()) return false;
            if (!railSpace.getType().isAir()) return false;
            if (!headSpace.getType().isAir()) return false;
        }
        return true;
    }

    private void buildRails(Location start) {
        World world = start.getWorld();
        int x = start.getBlockX();
        int y = start.getBlockY();
        int z = start.getBlockZ();

        for (int i = 0; i < 5; i++) {

            int dx = railDirection.getModX() * i;
            int dz = railDirection.getModZ() * i;

            Block railBlock = world.getBlockAt(x + dx, y, z + dz);
            railBlock.setType(Material.RAIL);

            placedBlocks.add(railBlock);
        }
    }

    private void spawnMinecart(Location loc) {
        StorageMinecart cart = (StorageMinecart) loc.getWorld().spawnEntity(loc, EntityType.MINECART_CHEST);
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
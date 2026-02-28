package subude.gg.Managers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SpawnManager {
    private final ConfigManager configManager;
    private final LootManager lootManager;
    private final Random random = new Random();
    private final List<Block> placedBlocks = new ArrayList<>();
    private final NamespacedKey key;
    private Location spawnLocation;
    private UUID minecartUUID;
    private BlockFace railDirection;
    private int currentStep;
    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    public SpawnManager(ConfigManager configManager, LootManager lootManager, JavaPlugin plugin) {
        this.configManager = configManager;
        this.lootManager = lootManager;
        this.key = new NamespacedKey(plugin,"cargo_minecart");
    }

    public boolean spawnStructure() {
        if (getMinecart() != null && !getMinecart().isDead()) {
            removeStructure();
        }

        Location loc = findSafeLocation();
        if (loc == null) return false;

        this.spawnLocation = loc;

        buildRails(loc);
        spawnMinecart(loc);
        currentStep = 1;

        playGlobalSound(configManager.spawnSounds, 8F, 1F);

        for (String startMessage : configManager.startMessage) {
            Bukkit.broadcastMessage(applyPlaceholders(startMessage));
        }

        return true;
    }

    private Location findSafeLocation() {
        if (getWorld() == null) return null;

        int radius = configManager.spawnRadius;
        int minY = configManager.minY;

        for (int attempt = 0; attempt < configManager.attemps; attempt++) {
            int x = random.nextInt(radius * 2) - radius;
            int z = random.nextInt(radius * 2) - radius;

            int y = getWorld().getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
            if (y < minY) continue;

            Location base = new Location(getWorld(), x, y, z);
            for (BlockFace face : FACES) {
                if (canPlaceRails(base, face)) {
                    railDirection = face;
                    return base.add(0.5, 1, 0.5);
                }
            }
        }
        return null;
    }

    private boolean canPlaceRails(Location start, BlockFace direction) {
        int x = start.getBlockX();
        int y = start.getBlockY();
        int z = start.getBlockZ();

        for (int i = 0; i < 5; i++) {

            int dx = direction.getModX() * i;
            int dz = direction.getModZ() * i;

            Block ground = getWorld().getBlockAt(x + dx, y, z + dz);
            Block railSpace = getWorld().getBlockAt(x + dx, y + 1, z + dz);
            Block headSpace = getWorld().getBlockAt(x + dx, y + 2, z + dz);

            if (!ground.getType().isSolid() || ground.getType() == Material.WATER || ground.getType() == Material.LAVA) return false;
            if (!railSpace.getType().isAir()) return false;
            if (!headSpace.getType().isAir()) return false;
        }
        return true;
    }

    private void buildRails(Location start) {
        int x = start.getBlockX();
        int y = start.getBlockY();
        int z = start.getBlockZ();

        for (int i = 0; i < 5; i++) {

            int dx = railDirection.getModX() * i;
            int dz = railDirection.getModZ() * i;

            Block railBlock = getWorld().getBlockAt(x + dx, y, z + dz);
            railBlock.setType(Material.RAIL);

            placedBlocks.add(railBlock);
        }
    }

    private void spawnMinecart(Location loc) {
        StorageMinecart cart = (StorageMinecart) getWorld().spawnEntity(loc, EntityType.MINECART_CHEST);
        minecartUUID = cart.getUniqueId();
        lootManager.fillMinecart(cart);

        cart.setCustomName("§6Маршруточный Груз: " + lootManager.getCargoType());
        cart.setCustomNameVisible(true);
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
        currentStep = 1;

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

        if (lootManager.getCargoType() != null) {
            message = message.replace("%type%", lootManager.getCargoType());
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void playGlobalSound(List<Sound> sounds, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sounds.stream().forEach(sound -> player.playSound(player.getLocation(),sound,volume,pitch));
        }
    }

    public StorageMinecart getMinecart() {
        if (minecartUUID == null) return null;

        Entity entity = getWorld().getEntity(minecartUUID);
        if (entity instanceof StorageMinecart cart) {
            return cart;
        }

        return null;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public BlockFace getRailDirection() {
        return railDirection;
    }

    private World getWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public void incrementSteps() {
        currentStep++;
    }
}
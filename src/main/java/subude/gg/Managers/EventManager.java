package subude.gg.Managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import java.util.Random;

public class EventManager {
    private final ConfigManager configManager;
    private final SpawnManager spawnManager;
    private final RandomStageEffectsManager randomStageEffectsManager;
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public EventManager(ConfigManager configManager, SpawnManager spawnManager, RandomStageEffectsManager randomStageEffectsManager, JavaPlugin plugin) {
        this.configManager = configManager;
        this.spawnManager = spawnManager;
        this.randomStageEffectsManager = randomStageEffectsManager;
        this.plugin = plugin;
    }

    public boolean nextCargoStage() {

        StorageMinecart cart = spawnManager.getMinecart();
        if (cart == null) return false;

        Location nextLoc = getNextLocation(cart.getLocation(), spawnManager.getRailDirection());
        cart.teleport(nextLoc);

        if (configManager.randomEffects) {
            randomStageEffectsManager.triggerRandomEffect();
        }

        randomStageEffectsManager.triggerStandartEffect();
        spawnManager.incrementSteps();

        configManager.stageSounds.forEach(sound ->
                cart.getWorld().playSound(nextLoc, sound, 8F, 1F));

        configManager.stageParticles.forEach(particle ->
                cart.getWorld().spawnParticle(particle, nextLoc, 200));

        return true;
    }

    private Location getNextLocation(Location loc, BlockFace direction) {
        Location next = loc.clone();

        switch (direction) {
            case NORTH -> next.add(0,0,-1);
            case SOUTH -> next.add(0,0,1);
            case WEST -> next.add(-1,0,0);
            case EAST -> next.add(1,0,0);
        }
        return next;
    }

    public void finishEvent() {

        StorageMinecart cart = spawnManager.getMinecart();
        if (cart == null) return;

        configManager.openSounds.forEach(sound ->
                cart.getWorld().playSound(cart.getLocation(), sound, 8F, 1F));

        configManager.openParticles.forEach(particle ->
                cart.getWorld().spawnParticle(particle, cart.getLocation(), 200));

        explodeLoot(cart);
    }

    private void explodeLoot(StorageMinecart cart) {
        Location center = cart.getLocation().clone().add(0, 0.5, 0);
        World world = center.getWorld();

        for (ItemStack item : cart.getInventory().getContents()) {
            if (item == null) continue;

            Item dropped = world.dropItem(center, item);
            dropped.setGlowing(true);

            Vector direction = new Vector(Math.random() - 0.5,Math.random() * 0.6 + 0.4,Math.random() - 0.5).normalize();

            direction.multiply(1.3);
            dropped.setVelocity(direction);
        }

        cart.getInventory().clear();
    }
}

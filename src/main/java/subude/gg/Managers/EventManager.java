package subude.gg.Managers;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;

public class EventManager {
    private final ConfigManager configManager;
    private final SpawnManager spawnManager;

    public EventManager(ConfigManager configManager, SpawnManager spawnManager) {
        this.configManager = configManager;
        this.spawnManager = spawnManager;
    }

    public void nextCargoStage() {
        Minecart cart = spawnManager.getMinecart();
        if (cart == null) return;

        int current = spawnManager.getCurrentStep();
        if (current >= 4) {
            finishEvent(cart);
            return;
        }

        Location nextLoc = getNextLocation(cart.getLocation(), spawnManager.getRailDirection());
        cart.teleport(nextLoc);
        spawnManager.incrementSteps();
        configManager.stageSounds.stream().forEach(sound -> cart.getWorld().playSound(nextLoc, sound, 8F, 1F));
        configManager.stageParticles.stream().forEach(particle -> cart.getWorld().spawnParticle(particle,nextLoc,200));
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

    private void finishEvent(Minecart cart) {
        configManager.openSounds.stream().forEach(sound -> cart.getWorld().playSound(cart.getLocation(),sound,8F,1F));
        configManager.openParticles.stream().forEach(particle -> cart.getWorld().spawnParticle(particle,cart.getLocation(),200));
        spawnManager.removeStructure();
    }
}

package subude.gg;

import org.bukkit.*;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Set;

public class CargoListener implements Listener {
    private final JavaPlugin plugin;
    private final Set<FallingBlock> activeMeteors;

    public CargoListener(JavaPlugin plugin, Set<FallingBlock> activeMeteors) {
        this.plugin = plugin;
        this.activeMeteors = activeMeteors;
    }

    @EventHandler
    public void onMeteorLand(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock)) return;

        FallingBlock fb = (FallingBlock) e.getEntity();
        if (fb.getBlockData().getMaterial() != Material.MAGMA_BLOCK) return;

        e.setCancelled(true);

        Location loc = fb.getLocation();
        World world = loc.getWorld();

        activeMeteors.remove(fb);
        fb.remove();

        world.createExplosion(loc, 3F, false, false);
        world.spawnParticle(Particle.EXPLOSION_HUGE, loc, 2);
        world.spawnParticle(Particle.FLAME, loc, 60, 1, 1, 1, 0.1);
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3F, 0.8F);
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent e) {
        if (!(e.getVehicle() instanceof StorageMinecart cart)) return;
        if (!cart.hasMetadata("cargo_cart")) return;

        cart.setVelocity(new Vector(0, 0, 0));
        cart.teleport(e.getFrom());
    }
}

package subude.gg.Managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class WorldGuardManager {
    private String currentRegionId;

    public void createEventRegion(Location center, int radius) {
        World world = center.getWorld();
        if (world == null) return;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions == null) return;

        String regionId = "cargo_" + UUID.randomUUID().toString().substring(0, 8);

        while (regions.hasRegion(regionId)) {
            regionId = "cargo_" + UUID.randomUUID().toString().substring(0, 8);
        }

        BlockVector3 min = BlockVector3.at(
                center.getBlockX() - radius,
                center.getBlockY() - radius,
                center.getBlockZ() - radius
        );
        BlockVector3 max = BlockVector3.at(
                center.getBlockX() + radius,
                world.getMaxHeight(),
                center.getBlockZ() + radius
        );

        ProtectedRegion region = new ProtectedCuboidRegion(regionId, min, max);

        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.TNT, StateFlag.State.DENY);
        region.setFlag(Flags.PISTONS, StateFlag.State.DENY);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);

        regions.addRegion(region);

        this.currentRegionId = regionId;
    }

    public void removeEventRegion(World world) {
        if (world == null) return;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions != null && regions.hasRegion(currentRegionId)) {
            regions.removeRegion(currentRegionId);
        }

        this.currentRegionId = null;
    }
}

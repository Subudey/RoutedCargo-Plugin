package subude.gg;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.minecart.StorageMinecart;
public class SpawnManager {
    private final ConfigManager configManager;
    private StorageMinecart minecart;

    public SpawnManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void spawnMinecart() {

    }


    public StorageMinecart getMinecart() {
        return minecart;
    }
}
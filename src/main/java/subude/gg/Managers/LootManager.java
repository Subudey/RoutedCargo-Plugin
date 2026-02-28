package subude.gg.Managers;

import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootManager {
    private final ConfigManager configManager;
    private final Random random = new Random();
    private ConfigManager.CargoType cargoType;

    public LootManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void fillMinecart(StorageMinecart cart) {
        cargoType = getRandomCargoType();
        if (cargoType == null) return;

        Inventory inv = cart.getInventory();

        for (ConfigManager.CargoItem cargoItem : cargoType.dropTable) {
            if (random.nextDouble() * 100 <= cargoItem.chance) {
                ItemStack item = new ItemStack(cargoItem.material, cargoItem.amount);
                inv.addItem(item);
            }
        }
    }


    private ConfigManager.CargoType getRandomCargoType() {
        if (configManager.cargoTypes.isEmpty()) return null;

        double totalChance = 0;

        for (ConfigManager.CargoType type : configManager.cargoTypes.values()) {
            if (type.chance > 0) {
                totalChance += type.chance;
            }
        }

        if (totalChance <= 0) {
            List<ConfigManager.CargoType> types = new ArrayList<>(configManager.cargoTypes.values());
            return types.get(random.nextInt(types.size()));
        }

        double randomValue = random.nextDouble() * totalChance;
        double current = 0;

        for (ConfigManager.CargoType type : configManager.cargoTypes.values()) {
            if (type.chance <= 0) continue;

            current += type.chance;
            if (randomValue <= current) {
                return type;
            }
        }

        return configManager.cargoTypes.values().iterator().next();
    }

    public String getCargoType() {
        return cargoType != null ? cargoType.name : null;
    }
}

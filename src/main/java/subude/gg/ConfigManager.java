package subude.gg;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;

    public int eventInterval;
    public int eventStartInterval;
    public int eventDuration;
    public String stageSound;
    public int spawnRadius;
    public int minY;
    public List<String> startMessage;
    public List<String> endMessage;

    public final Map<String, CargoType> cargoTypes = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadConfig();
    }

    private void loadConfig() {
        eventInterval = plugin.getConfig().getInt("event-interval");
        eventStartInterval = plugin.getConfig().getInt("event-start-interval");
        eventDuration = plugin.getConfig().getInt("event-duration");
        stageSound = plugin.getConfig().getString("stage-sound");
        spawnRadius = plugin.getConfig().getInt("spawn-radius");
        minY = plugin.getConfig().getInt("min-y");
        startMessage = plugin.getConfig().getStringList("start-message");
        endMessage = plugin.getConfig().getStringList("end-message");

        ConfigurationSection typesSection = plugin.getConfig().getConfigurationSection("cargo-types");
        if (typesSection != null) {

            for (String key : typesSection.getKeys(false)) {
                ConfigurationSection typeSection = typesSection.getConfigurationSection(key);
                CargoType type = new CargoType();
                type.name = typeSection.getString("name");
                type.chance = typeSection.getDouble("chance");
                type.dropTable = new ArrayList<>();
                List<Map<?, ?>> drops = typeSection.getMapList("drop-table");

                for (Map<?, ?> drop : drops) {
                    CargoItem item = new CargoItem();
                    item.material = Material.valueOf(drop.get("material").toString());
                    item.amount = (int) drop.get("amount");
                    item.chance = (double) drop.get("chance");
                    type.dropTable.add(item);
                }
                cargoTypes.put(key, type);
            }
        }
    }

    public void reload() {
        plugin.reloadConfig();
        cargoTypes.clear();
        loadConfig();
    }

    public static class CargoType {
        public String name;
        public double chance;
        public List<CargoItem> dropTable;
    }

    public static class CargoItem {
        public Material material;
        public int amount;
        public double chance;
    }
}

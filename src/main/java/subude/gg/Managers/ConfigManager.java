package subude.gg.Managers;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {
    public int eventInterval;
    public int eventStartInterval;
    public int eventDuration;
    public int attemps;
    public int spawnRadius;
    public int minY;
    public boolean randomEffects;
    public List<Sound> spawnSounds;
    public List<Sound> stageSounds;
    public List<Sound> openSounds;
    public List<Particle> stageParticles;
    public List<Particle> openParticles;
    public List<String> startMessage;
    public List<String> endMessage;
    public List<String> statusGoMessage;
    public List<String> statusNoneMessage;
    public final Map<String, CargoType> cargoTypes = new HashMap<>();
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadConfig();
    }

    private void loadConfig() {
        try {
            eventInterval = plugin.getConfig().getInt("event-interval");
            eventStartInterval = plugin.getConfig().getInt("event-start-interval");
            eventDuration = plugin.getConfig().getInt("event-duration");
            spawnRadius = plugin.getConfig().getInt("spawn-radius");
            minY = plugin.getConfig().getInt("min-y");
            randomEffects = plugin.getConfig().getBoolean("random-effects");
            startMessage = plugin.getConfig().getStringList("start-message");
            endMessage = plugin.getConfig().getStringList("end-message");
            statusGoMessage = plugin.getConfig().getStringList("status-go-message");
            statusNoneMessage = plugin.getConfig().getStringList("status-none-message");
            attemps = plugin.getConfig().getInt("event-attemps");
            spawnSounds = plugin.getConfig().getStringList("spawn-sounds").stream().map(s -> Sound.valueOf(s)).collect(Collectors.toList());
            stageSounds = plugin.getConfig().getStringList("stage-sounds").stream().map(s -> Sound.valueOf(s)).collect(Collectors.toList());
            openSounds = plugin.getConfig().getStringList("open-sounds").stream().map(s -> Sound.valueOf(s)).collect(Collectors.toList());
            stageParticles = plugin.getConfig().getStringList("stage-particles").stream().map(s -> Particle.valueOf(s)).collect(Collectors.toList());
            openParticles = plugin.getConfig().getStringList("open-particles").stream().map(s -> Particle.valueOf(s)).collect(Collectors.toList());

            ConfigurationSection typesSection = plugin.getConfig().getConfigurationSection("cargo-types");
            if (typesSection != null) {
                for (String key : typesSection.getKeys(false)) {
                    ConfigurationSection typeSection = typesSection.getConfigurationSection(key);
                    CargoType type = new CargoType();
                    type.name = typeSection.getString("name");
                    type.chance = typeSection.getInt("chance");
                    type.dropTable = new ArrayList<>();
                    List<Map<?, ?>> drops = typeSection.getMapList("drop-table");

                    for (Map<?, ?> drop : drops) {
                        CargoItem item = new CargoItem();
                        item.material = Material.valueOf(drop.get("material").toString());
                        item.amount = (int) drop.get("amount");
                        item.chance = (int) drop.get("chance");
                        type.dropTable.add(item);
                    }
                    cargoTypes.put(key, type);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Некорректные значения в config.yml!");
        }

    }

    public void reload() {
        plugin.reloadConfig();
        cargoTypes.clear();
        loadConfig();
    }

    public static class CargoType {
        public String name;
        public int chance;
        public List<CargoItem> dropTable;
    }

    public static class CargoItem {
        public Material material;
        public int amount;
        public int chance;
    }
}

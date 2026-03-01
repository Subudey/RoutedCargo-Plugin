package subude.gg.Managers;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomStageEffectsManager {
    private final SpawnManager spawnManager;
    private final Set<FallingBlock> activeMeteors = new HashSet<>();
    private final LootManager lootManager;
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public RandomStageEffectsManager(SpawnManager spawnManager, LootManager lootManager, JavaPlugin plugin) {
        this.spawnManager = spawnManager;
        this.lootManager = lootManager;
        this.plugin = plugin;
    }

    public enum StageEffectType {POISON_RADIUS, KNOCKBACK_RADIUS, SPAWN_MOBS, SPAWN_METEOR, DROP_ITEM}

    private EntityType getRandomMobType() {
        EntityType[] types = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.DROWNED};
        return types[random.nextInt(types.length)];
    }

    private void equipArmorByChance(LivingEntity mob, int chance) {
        EntityEquipment eq = mob.getEquipment();
        if (eq == null) return;

        if (chance <= 10) {
            eq.setHelmet(new ItemStack(Material.IRON_HELMET));
            eq.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            eq.setBoots(new ItemStack(Material.IRON_BOOTS));

        } else if (chance <= 50) {
            eq.setHelmet(new ItemStack(Material.LEATHER_HELMET));
            eq.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            eq.setBoots(new ItemStack(Material.LEATHER_BOOTS));
        }

        eq.setHelmetDropChance(0f);
        eq.setChestplateDropChance(0f);
        eq.setLeggingsDropChance(0f);
        eq.setBootsDropChance(0f);
    }

    private void trackMeteor(FallingBlock meteor) {
        new BukkitRunnable() {
            double rotation = 0;

            @Override
            public void run() {
                if (!meteor.isValid()) {
                    cancel();
                    return;
                }

                Location loc = meteor.getLocation();
                World world = loc.getWorld();

                rotation += 0.6;

                double spiralX = Math.cos(rotation) * 0.18;
                double spiralZ = Math.sin(rotation) * 0.18;

                Vector current = meteor.getVelocity();
                meteor.setVelocity(new Vector(spiralX,current.getY() - 0.002, spiralZ));

                world.spawnParticle(Particle.FLAME, loc, 8, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.SMOKE_LARGE, loc, 6, 0.15, 0.15, 0.15, 0);
                world.spawnParticle(Particle.LAVA, loc, 3, 0.1, 0.1, 0.1, 0);

                for (int i = 1; i <= 3; i++) {
                    Location trail = loc.clone().subtract(0, i * 0.5, 0);
                    world.spawnParticle(Particle.SMOKE_NORMAL, trail, 1, 0, 0, 0, 0);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void triggerStandartEffect() {
        StorageMinecart cart = spawnManager.getMinecart();
        if (cart == null) return;

        for (Player player : cart.getWorld().getPlayers()) {
            if (player.getLocation().distance(cart.getLocation()) <= 25) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20,0));
            }
        }
    }

    public void triggerRandomEffect() {
        StorageMinecart cart = spawnManager.getMinecart();
        if (cart == null) return;

        StageEffectType[] values = StageEffectType.values();
        StageEffectType chosen = values[random.nextInt(values.length)];

        switch (chosen) {
            case POISON_RADIUS -> poisonPlayers(cart);
            case KNOCKBACK_RADIUS -> knockbackPlayers(cart);
            case SPAWN_MOBS -> spawnMobs(cart);
            case SPAWN_METEOR -> spawnMeteor(cart);
            case DROP_ITEM -> dropItems(cart);
        }
    }

    private void poisonPlayers(Minecart cart) {
        for (Player player : cart.getWorld().getPlayers()) {
            if (player.getLocation().distance(cart.getLocation()) <= 25) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON,200,0));
            }
        }
    }

    private void knockbackPlayers(StorageMinecart cart) {
        for (Player player : cart.getWorld().getPlayers()) {
            if (player.getLocation().distance(cart.getLocation()) <= 25) {
                Vector direction = player.getLocation().toVector().subtract(cart.getLocation().toVector()).normalize().multiply(1.8);
                direction.setY(1.2);
                player.setVelocity(direction);
            }
        }
    }

    private void spawnMobs(Minecart cart) {
        Location center = cart.getLocation();
        World world = center.getWorld();
        int amount = 8;

        int chance = lootManager.getCargoType().chance;

        for (int i = 0; i < amount; i++) {
            EntityType type = getRandomMobType();
            LivingEntity mob = (LivingEntity) world.spawnEntity(center, type);

            mob.setSilent(true);

            if (mob instanceof Zombie zombie) {
                zombie.setShouldBurnInDay(false);
            }

            if (mob instanceof Skeleton skeleton) {
                skeleton.setShouldBurnInDay(false);
            }

            equipArmorByChance(mob, chance);

            double angle = random.nextDouble() * 2 * Math.PI;
            Vector velocity = new Vector(Math.cos(angle) * 1.2,0.7,Math.sin(angle) * 1.2);
            mob.setVelocity(velocity);
        }
    }

    private void spawnMeteor(StorageMinecart cart) {
        int amount = 12;
        double radius = 20;
        Location center = cart.getLocation();
        World world = center.getWorld();

        new BukkitRunnable() {
            int spawned = 0;

            @Override
            public void run() {
                if (spawned >= amount) {
                    cancel();
                    return;
                }

                double angle = random.nextDouble() * 2 * Math.PI;
                double randomRadius = radius * Math.sqrt(random.nextDouble());

                double x = center.getX() + Math.cos(angle) * randomRadius;
                double z = center.getZ() + Math.sin(angle) * randomRadius;
                double y = center.getY() + 50;

                Location spawnLoc = new Location(world, x, y, z);

                FallingBlock meteor = world.spawnFallingBlock(spawnLoc, Material.MAGMA_BLOCK.createBlockData());
                activeMeteors.add(meteor);

                meteor.setDropItem(false);
                meteor.setHurtEntities(false);
                meteor.setVelocity(new Vector(0, -0.05, 0));
                trackMeteor(meteor);

                spawned++;
            }
        }.runTaskTimer(plugin, 0L, 12L);
    }

    private void dropItems(StorageMinecart cart) {
        Location center = cart.getLocation();

        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distance(center) > 25) continue;

            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem.getType() == Material.AIR) continue;

            ItemStack dropStack = handItem.clone();
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

            Location dropLoc = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(0.5));

            Item dropped = player.getWorld().dropItem(dropLoc, dropStack);

            Vector direction = player.getLocation().getDirection().normalize();

            Vector velocity = direction.multiply(0.8);
            velocity.setY(0.2);

            dropped.setVelocity(velocity);
            dropped.setPickupDelay(20);
        }
    }

    public Set<FallingBlock> getActiveMeteors() {
        return activeMeteors;
    }
}

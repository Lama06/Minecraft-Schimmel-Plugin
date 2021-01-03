package io.github.lama06.schimmel;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class Schimmel extends JavaPlugin implements Listener {

    // Gibt an ob das Plugin aktuell aktiviert ist
    private boolean enabled = false;

    // Blockarten die von Schimmel betroffen sein können.
    private final HashSet<Material> possibleMoldyBlocks = new HashSet<>(Arrays.asList(
            Material.STONE,
            Material.STONE_SLAB,
            Material.STONE_STAIRS,

            Material.COBBLESTONE,
            Material.COBBLESTONE_SLAB,
            Material.COBBLESTONE_STAIRS,

            Material.STONE_BRICKS,
            Material.STONE_BRICK_SLAB,
            Material.STONE_BRICK_STAIRS,

            Material.SPRUCE_LOG,
            Material.SPRUCE_PLANKS,
            Material.SPRUCE_SLAB,
            Material.SPRUCE_STAIRS,

            Material.BIRCH_LOG,
            Material.BIRCH_PLANKS,
            Material.BIRCH_SLAB,
            Material.BIRCH_STAIRS,

            Material.OAK_LOG,
            Material.OAK_PLANKS,
            Material.OAK_SLAB,
            Material.OAK_STAIRS,

            Material.DARK_OAK_LOG,
            Material.DARK_OAK_PLANKS,
            Material.DARK_OAK_SLAB,
            Material.DARK_OAK_STAIRS,

            Material.ACACIA_LOG,
            Material.ACACIA_PLANKS,
            Material.ACACIA_SLAB,
            Material.ACACIA_STAIRS,

            Material.JUNGLE_LOG,
            Material.JUNGLE_PLANKS,
            Material.JUNGLE_SLAB,
            Material.JUNGLE_STAIRS
    ));

    // Gibt an, in welchen Block sich ein Block während seines Schimmelprozesses umwandelt
    private final Map<Material, Material> moldyBlockVariants = new HashMap<Material, Material>() {{
        put(Material.STONE, Material.STONE_STAIRS);
        put(Material.STONE_STAIRS, Material.STONE_SLAB);

        put(Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        put(Material.MOSSY_COBBLESTONE, Material.MOSSY_COBBLESTONE_SLAB);

        put(Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS);
        put(Material.MOSSY_STONE_BRICKS, Material.MOSSY_STONE_BRICK_SLAB);

        put(Material.SPRUCE_LOG, Material.SPRUCE_SLAB);
        put(Material.SPRUCE_PLANKS, Material.SPRUCE_SLAB);

        put(Material.BIRCH_LOG, Material.BIRCH_SLAB);
        put(Material.BIRCH_PLANKS, Material.BIRCH_SLAB);

        put(Material.OAK_LOG, Material.OAK_SLAB);
        put(Material.OAK_PLANKS, Material.OAK_SLAB);

        put(Material.DARK_OAK_LOG, Material.DARK_OAK_SLAB);
        put(Material.DARK_OAK_PLANKS, Material.DARK_OAK_SLAB);

        put(Material.ACACIA_LOG, Material.ACACIA_SLAB);
        put(Material.ACACIA_PLANKS, Material.ACACIA_SLAB);

        put(Material.JUNGLE_LOG, Material.JUNGLE_SLAB);
        put(Material.JUNGLE_PLANKS, Material.JUNGLE_SLAB);
    }};

    // Liste der Blöcke, die aktuell schimmeln
    private final Vector<Block> moldyBlocks = new Vector<>();





    // Wenn ein Spieler Wasser setzt, wird an diesem Ort Schimmel ausgelöst
    @EventHandler
    public void onPlayerPlacesWater(PlayerBucketEmptyEvent e) {
        if(enabled && e.getBucket() == Material.WATER_BUCKET && possibleMoldyBlocks.contains(e.getBlockClicked().getType())) {
            moldyBlocks.add(e.getBlockClicked());
        }
    }

    //Wenn ein Spieler Lava setzt, wird an diesem Ort Schimmel entfernt
    @EventHandler
    public void onPlayerPlacesLava(PlayerBucketEmptyEvent e) {
        if(enabled && e.getBucket() == Material.LAVA_BUCKET) {
            moldyBlocks.remove(e.getBlockClicked());
        }
    }

    // Wenn ein schimmelnder Block abgebaut wird muss dieser aus der Liste der schimmelnder Blöcke entfernt werden
    @EventHandler
    public void onPlayerBreaksMoldyBlock(BlockBreakEvent e) {
        Block block = e.getBlock();

        if(enabled && moldyBlocks.contains(block)) {
            moldyBlocks.remove(block);
            block.getWorld().spawnEntity(block.getLocation(), EntityType.SILVERFISH).setCustomName("Ratte");
        }
    }

    // Wenn ein Spieler sich auf einen verschimmelten Block bewegt, dem Spieler Vergiftung geben
    @EventHandler
    public void onPlayerMovesOnMoldyBlock(PlayerMoveEvent e) {
        // Block unter dem Spieler erhalten
        Location location = e.getPlayer().getLocation();
        location.setY(location.getY() - 1);
        Block block = e.getPlayer().getWorld().getBlockAt(location);

        if(enabled && moldyBlocks.contains(block)) {
            Player player = e.getPlayer();
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1));
        }
    }





    // Steuert die zufälligen Folgen des Schimmels
    private void controlMouldImpactTask() {
        if(!enabled) return;

        Random rnd = new Random();

        // Durch alle schimmelnden Blöcke gehen und zufällige Folgen des Schimmels steuern
        for(Iterator<Block> iterator = moldyBlocks.iterator(); iterator.hasNext();) {
            Block block = iterator.next();

            if(rnd.nextInt(8) == 1) {
                // "Ratten spawnen"
                Location location = block.getLocation();
                location.setY(location.getY() + 1);
                block.getWorld().spawnEntity(location, EntityType.SILVERFISH).setCustomName("Ratte");
            }

            if(rnd.nextInt(10) == 1) {
                // Block zu Slab konvertieren und Slab zu Air konvertieren
                if(moldyBlockVariants.containsKey(block.getType())) {
                    block.setType(moldyBlockVariants.get(block.getType()));
                } else {
                    iterator.remove();
                    block.setType(Material.AIR);
                }
            }
        }
    }






    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(cmd.getName().equals("schimmel")) {
            if(args.length == 0) {
                if(enabled) {
                    sender.sendMessage("Das Plugin ist zurzeit aktiviert");
                } else {
                    sender.sendMessage("Das Plugin ist zurzeit nicht aktiviert");
                }
            } else if(args[0].equals("an")) {
                enabled = true;
                sender.sendMessage("Das Plugin wurde nun aktiviert");
            } else if(args[0].equals("aus")) {
                enabled = false;
                moldyBlocks.clear();
                sender.sendMessage("Das Plugin wurde nun deaktiviert");
            } else if(args[0].equals("reset")) {
                moldyBlocks.clear();
                sender.sendMessage("Alle schimmelnden Blöcke wurden zurückgesetzt");
            }
        } else if(cmd.getName().equals("schimmelcheck")) {
            if(sender instanceof Player) {
                Player player = (Player) sender;
                if(moldyBlocks.contains(player.getTargetBlock(5))) {
                    player.sendMessage("Dieser Block schimmelt");
                } else {
                    player.sendMessage("Dieser Block schimmelt nicht");
                }
            } else {
                sender.sendMessage("Dieser Command kann nicht von in der Konsole ausgeführt werden");
            }
        }

        return true;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::controlMouldImpactTask, 0L, 60L);
    }
}

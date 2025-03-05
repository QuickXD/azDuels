package it.quick.azduels.managers;

import it.quick.azduels.AzDuels;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DuelManager implements Listener {
    private final AzDuels plugin;
    private final HashMap<UUID, UUID> duelRequests = new HashMap<>();
    private final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final HashMap<UUID, Location> savedLocations = new HashMap<>();

    private Player activeDueler1;
    private Player activeDueler2;

    public DuelManager(AzDuels plugin) {
        this.plugin = plugin;
    }

    public void sendDuelRequest(Player sender, Player target) {
        duelRequests.put(target.getUniqueId(), sender.getUniqueId());
        sender.sendMessage("§aRichiesta di duello inviata a " + target.getName());
        target.sendMessage("§e" + sender.getName() + " ti ha sfidato! Digita /duel accept o /duel refuse.");
    }

    public void acceptDuel(Player player) {
        if (!duelRequests.containsKey(player.getUniqueId())) {
            player.sendMessage("§cNessuna richiesta in sospeso.");
            return;
        }
        Player sender = Bukkit.getPlayer(duelRequests.remove(player.getUniqueId()));
        if (sender == null) return;

        activeDueler1 = sender;
        activeDueler2 = player;

        savedInventories.put(sender.getUniqueId(), sender.getInventory().getContents());
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents());

        savedLocations.put(sender.getUniqueId(), sender.getLocation());
        savedLocations.put(player.getUniqueId(), player.getLocation());

        resetPlayerState(sender);
        resetPlayerState(player);

        sender.teleport(getDuelLocation("duel.position1"));
        player.teleport(getDuelLocation("duel.position2"));

        giveKit(sender);
        giveKit(player);

        sender.sendMessage("§6Duello iniziato con " + player.getName());
        player.sendMessage("§6Duello iniziato con " + sender.getName());
    }

    public void refuseDuel(Player player) {
        duelRequests.remove(player.getUniqueId());
        player.sendMessage("§cHai rifiutato il duello.");
    }

    private Location getDuelLocation(String path) {
        FileConfiguration config = plugin.getConfig();
        return new Location(
                Bukkit.getWorld(config.getString(path + ".world")),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z")
        );
    }

    private void resetPlayerState(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
    }

    private void giveKit(Player player) {
        player.getInventory().clear();
        FileConfiguration config = plugin.getConfig();

        player.getInventory().setHelmet(new ItemStack(Material.valueOf(config.getString("kit.helmet", "DIAMOND_HELMET"))));
        player.getInventory().setChestplate(new ItemStack(Material.valueOf(config.getString("kit.chestplate", "DIAMOND_CHESTPLATE"))));
        player.getInventory().setLeggings(new ItemStack(Material.valueOf(config.getString("kit.leggings", "DIAMOND_LEGGINGS"))));
        player.getInventory().setBoots(new ItemStack(Material.valueOf(config.getString("kit.boots", "DIAMOND_BOOTS"))));

        List<String> items = config.getStringList("kit.items");
        for (String item : items) {
            String[] parts = item.split(":");
            Material material = Material.valueOf(parts[0].toUpperCase());
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            player.getInventory().addItem(new ItemStack(material, amount));
        }
    }

    public void endDuel(Player winner, Player loser) {
        winner.sendMessage("§aHai vinto!");
        loser.sendMessage("§cHai perso!");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            restorePlayer(winner);
            restorePlayer(loser);
            activeDueler1 = null;
            activeDueler2 = null;
        }, 20L);
    }

    private void restorePlayer(Player player) {
        if (savedInventories.containsKey(player.getUniqueId())) {
            player.getInventory().clear();
            player.getInventory().setContents(savedInventories.remove(player.getUniqueId()));
        }

        if (savedLocations.containsKey(player.getUniqueId())) {
            Location savedLocation = savedLocations.remove(player.getUniqueId());
            if (savedLocation != null) {
                player.teleport(savedLocation);
            }
        }

        player.sendMessage("§aIl tuo inventario e la tua posizione sono stati ripristinati!");
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (activeDueler1 != null && activeDueler2 != null) {
            if (event.getPlayer().equals(activeDueler1) || event.getPlayer().equals(activeDueler2)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (activeDueler1 != null && activeDueler2 != null) {
            if (event.getWhoClicked().equals(activeDueler1) || event.getWhoClicked().equals(activeDueler2)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.equals(activeDueler1) || player.equals(activeDueler2)) {
            event.getDrops().clear();
            event.setKeepInventory(true);
            event.setDeathMessage(null);

            if (savedLocations.containsKey(player.getUniqueId())) {
                player.teleport(savedLocations.get(player.getUniqueId()));
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (player.equals(activeDueler1) || player.equals(activeDueler2)) {
            if (savedLocations.containsKey(player.getUniqueId())) {
                Location savedLocation = savedLocations.get(player.getUniqueId());
                event.setRespawnLocation(savedLocation);
            }
        }
    }
}

package it.quick.azduels.managers;

import it.quick.azduels.AzDuels;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseManager {

    private final AzDuels plugin;
    private Connection connection;

    public DatabaseManager(AzDuels plugin) {
        this.plugin = plugin;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/duels.db");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS inventories (uuid TEXT PRIMARY KEY, items TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void savePlayerInventory(Player player) {
        String serializedItems = InventoryManager.serializeInventory(player.getInventory());
        try (PreparedStatement stmt = connection.prepareStatement("REPLACE INTO inventories (uuid, items) VALUES (?, ?)")) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, serializedItems);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restorePlayerInventory(Player player) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT items FROM inventories WHERE uuid = ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                InventoryManager.deserializeInventory(player.getInventory(), rs.getString("items"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null) connection.close();
        } catch (Exception ignored) {}
    }
}

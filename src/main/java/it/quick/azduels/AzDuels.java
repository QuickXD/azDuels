package it.quick.azduels;

import it.quick.azduels.commands.DuelCommand;
import it.quick.azduels.listeners.PlayerDeathListener;
import it.quick.azduels.managers.DuelManager;
import it.quick.azduels.managers.InventoryManager;
import it.quick.azduels.managers.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AzDuels extends JavaPlugin {
    private DuelManager duelManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        databaseManager = new DatabaseManager(this);
        duelManager = new DuelManager(this);

        getCommand("duel").setExecutor(new DuelCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(duelManager), this);
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
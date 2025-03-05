package it.quick.azduels.listeners;

import it.quick.azduels.managers.DuelManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    private final DuelManager duelManager;

    public PlayerDeathListener(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player loser = event.getEntity();
        Player winner = loser.getKiller();

        if (winner != null) {
            duelManager.endDuel(winner, loser);
        }
    }
}

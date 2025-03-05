package it.quick.azduels.commands;

import it.quick.azduels.AzDuels;
import it.quick.azduels.managers.DuelManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {
    private final DuelManager duelManager;

    public DuelCommand(AzDuels plugin) {
        this.duelManager = plugin.getDuelManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("accept")) {
                duelManager.acceptDuel(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("refuse")) {
                duelManager.refuseDuel(player);
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || target == player) {
                player.sendMessage("§cGiocatore non trovato!");
                return true;
            }
            duelManager.sendDuelRequest(player, target);
            return true;
        }
        player.sendMessage("§cUsa: /duel <player> | /duel accept | /duel refuse");
        return true;
    }
}
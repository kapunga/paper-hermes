package org.kapunga.hermes.link;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HermesLinkCommand implements CommandExecutor {
    private final PlayerLinkService playerLinkService;

    public HermesLinkCommand(PlayerLinkService playerLinkService) {
        this.playerLinkService = playerLinkService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "accept" -> {
                        doAccept((Player) sender);
                        return true;
                    }
                    case "deny" -> {
                        doDeny((Player) sender);
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            sender.sendPlainMessage("'hermeslink' can only be used by a player.");
            return true;
        }
    }

    private void doAccept(Player player) {
        if (playerLinkService.acceptLink(player)) {
            player.sendPlainMessage("Successfully accepted Slack link.");
        } else {
            player.sendPlainMessage("Error accepting Slack link, please try again.");
        }
    }

    private void doDeny(Player player) {
        if (playerLinkService.denyLink(player)) {
            player.sendPlainMessage("Slack link denied.");
        } else {
            player.sendPlainMessage("Error denying Slack link, request will timeout.");
        }
    }
}

package org.kapunga.hermes.link;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.kapunga.hermes.model.PlayerStub;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLinkService {
    YamlConfiguration playerLinkConf;
    List<String> groups;

    Map<UUID, CompletableFuture<Boolean>> linkRequests = new ConcurrentHashMap<>();
    public PlayerLinkService(FileConfiguration config, File pluginData) {
        File playerLinkFile = new File(pluginData, "playerLink.yml");

        if (!pluginData.exists()) {
            pluginData.mkdirs();
        }

        groups = config.getStringList("config-groups");
        playerLinkConf = YamlConfiguration.loadConfiguration(playerLinkFile);

        groups.forEach(group -> Bukkit.getLogger().info("Found group: " + group));
    }

    public List<PlayerStub> listPlayers() {
        return Bukkit.getOnlinePlayers()
                .stream().map(PlayerStub::new)
                .toList();
    }

    public List<String> listGroups() {
        return groups;
    }

    public CompletableFuture<Boolean> requestLinkPlayer(PlayerLinkRequest req) {
        Bukkit.getLogger().info("Attempting to link " +
                req.id() + " to " + req.slackId() + " with group " + req.group());

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Player player = Bukkit.getPlayer(req.id());

        if (player != null && player.isOnline()) {
            linkRequests.put(req.id(), future);
            future.whenComplete((b, e) -> {
                if (b != null) {
                    Bukkit.getLogger().info("Completing Future: " + b);
                } else if (e != null) {
                    Bukkit.getLogger().warning("Future failed: " + e);
                }

                linkRequests.remove(req.id());
            });

            TextComponent linkMsg = Component.text("Link to Slack User '" +
                    req.slackName() + "' and group '" + req.group() + "'?");

            TextComponent accept = Component.text("[Accept]")
                    .color(TextColor.color(0x00ff00))
                    .clickEvent(ClickEvent.runCommand("/hermeslink accept"));

            TextComponent reject = Component.text("[Reject]")
                    .color(TextColor.color(0xff0000))
                    .clickEvent(ClickEvent.runCommand("/hermeslink deny"));

            Component message = Component.join(JoinConfiguration.separator(Component.space()), linkMsg, accept, reject);

            player.sendMessage(message);
        } else {
            future.complete(false);
        }

        return future;
    }

    public boolean acceptLink(Player player) {
        CompletableFuture<Boolean> future = linkRequests.get(player.getUniqueId());

        if (future != null) {
            future.complete(true);
            return true;
        } else {
            return false;
        }
    }

    public boolean denyLink(Player player) {
        CompletableFuture<Boolean> future = linkRequests.get(player.getUniqueId());

        if (future != null) {
            future.complete(false);
            return true;
        } else {
            return false;
        }
    }
}

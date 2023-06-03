package org.kapunga.hermes.link;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.kapunga.hermes.model.HermesMsg;
import org.kapunga.hermes.model.PlayerStub;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PlayerLinkService {
    Logger logger;
    SlackLinkConfig slackLinkConfig;
    LuckPerms luckPermsApi;
    List<String> groups;

    Map<UUID, PendingLinkRequest> linkRequests = new ConcurrentHashMap<>();
    public PlayerLinkService(FileConfiguration config, File pluginData, LuckPerms luckPermsApi) {
        this.logger = Bukkit.getLogger();
        this.luckPermsApi = luckPermsApi;

        slackLinkConfig = new SlackLinkConfig(pluginData);

        try {
            List<String> configGroups = config.getStringList("config-groups");
            luckPermsApi.getGroupManager().loadAllGroups().get();
            Set<String> lpGroups = luckPermsApi.getGroupManager()
                    .getLoadedGroups()
                    .stream().map(Group::getName).collect(Collectors.toSet());

            groups = configGroups.stream().filter(g -> {
                if (lpGroups.contains(g.toLowerCase())) {
                    return true;
                } else {
                    logger.warning("Config group '" + g + "' not found in permissions.");
                    return false;
                }
            }).toList();
        } catch (Exception e) {
            logger.warning("PlayerLinkService initialization interrupted while loading.");
        }

        groups.forEach(group -> logger.info("Found group: " + group));
    }

    public List<PlayerStub> listUnlinkedPlayers() {
        logger.info("Request for players list.");

        var linkedPlayers = slackLinkConfig.getLinkedPlayers();

        return Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> !linkedPlayers.contains(p.getUniqueId()))
                .map(PlayerStub::new)
                .toList();
    }

    public List<String> listGroups() {
        logger.info("Request for groups list");
        return groups;
    }

    public CompletableFuture<Void> requestLinkPlayer(PlayerLinkRequest req) {
        logger.info("Attempting to link " + req.id() + " to " +
                req.slackId() + " with group " + req.group());

        CompletableFuture<Void> future = new CompletableFuture<>();

        Player player = Bukkit.getPlayer(req.id());

        if (player == null || !player.isOnline()) {
            future.completeExceptionally(new NotFoundResponse("Player not found!"));
        } else if (slackLinkConfig.isPlayerLinked(player.getUniqueId())) {
            future.completeExceptionally(new ConflictResponse("Player is already linked!"));
        } else if (groups.stream().anyMatch(g -> g.equalsIgnoreCase(req.group()))) {
            linkRequests.put(req.id(), new PendingLinkRequest(req, future));
            future.whenComplete((b, e) -> linkRequests.remove(req.id()));

            final var message = HermesMsg.linkRequest(req.slackName(), req.group());

            player.sendMessage(message);
        } else {
            future.completeExceptionally(new BadRequestResponse("Invalid group: " + req.group()));
        }

        return future;
    }

    public boolean acceptLink(Player player) {
        PendingLinkRequest pendingLinkRequest = linkRequests.get(player.getUniqueId());
        User user = luckPermsApi.getPlayerAdapter(Player.class).getUser(player);

        try {
            logger.info("User's current group: " + user.getPrimaryGroup());
            var group = pendingLinkRequest.req().group().toLowerCase();
            logger.info("Attempting to set group: " + group);

            addUserToGroup(player, group).join();
            slackLinkConfig.setPlayerSlackUserId(player.getUniqueId(), pendingLinkRequest.req().slackId());

            logger.info("User's new group: " + user.getPrimaryGroup());
            pendingLinkRequest.future().complete(null);
            return true;
        } catch (IllegalStateException e) {
            pendingLinkRequest.future().completeExceptionally(new InternalError("Bad Permission State"));
            return false;
        } catch (NullPointerException e) {
            pendingLinkRequest.future().completeExceptionally(new InternalError("Missing Permission State"));
            return false;
        } catch (CompletionException e) {
            pendingLinkRequest.future().completeExceptionally(new InternalError("Failed writing permissions."));
            return false;
        }
    }

    public boolean denyLink(Player player) {
        PendingLinkRequest req = linkRequests.get(player.getUniqueId());

        if (req != null) {
            req.future().completeExceptionally(new ForbiddenResponse("Link denied by player."));
            return true;
        } else {
            return false;
        }
    }

    private CompletableFuture<Void> addUserToGroup(Player player, String group) {
        return luckPermsApi.getUserManager().modifyUser(player.getUniqueId(), (User user) -> {
            user.data().clear(NodeType.INHERITANCE::matches);
            Node node = InheritanceNode.builder(group).build();
            user.data().add(node);
        });
    }
}

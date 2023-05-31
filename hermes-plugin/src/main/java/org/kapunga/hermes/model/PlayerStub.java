package org.kapunga.hermes.model;

import org.bukkit.entity.Player;

import java.util.UUID;

public record PlayerStub(String name, UUID id, String slackUserId) {
    public PlayerStub(Player player) {
        this(player.getName(), player.getUniqueId(), null);
    }

    public PlayerStub withSlackId(String slackUserId) {
        return new PlayerStub(this.name, this.id, slackUserId);
    }
}

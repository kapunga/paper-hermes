package org.kapunga.hermes.model;

import org.bukkit.entity.Player;

import java.util.UUID;

public record PlayerStub(String name, UUID id) {
    public PlayerStub(Player player) {
        this(player.getName(), player.getUniqueId());
    }
}

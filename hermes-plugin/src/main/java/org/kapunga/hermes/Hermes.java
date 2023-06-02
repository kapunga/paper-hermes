package org.kapunga.hermes;

import io.javalin.Javalin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.kapunga.hermes.api.ApiBuilder;
import org.kapunga.hermes.link.HermesLinkCommand;
import org.kapunga.hermes.link.PlayerLinkService;

import java.util.Objects;

public class Hermes extends JavaPlugin implements Listener {
    PlayerLinkService playerLinkService;
    LuckPerms luckPermsApi;
    Javalin endpoints;

    @Override
    public void onEnable() {
        luckPermsApi = Objects.requireNonNull(
                Bukkit.getServicesManager().getRegistration(LuckPerms.class)).getProvider();

        playerLinkService = new PlayerLinkService(getConfig(), getDataFolder(), luckPermsApi);

        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("hermeslink")).setExecutor(new HermesLinkCommand(playerLinkService));
        Bukkit.getLogger().info("Hermes Plugin Started");

        endpoints = ApiBuilder.create(playerLinkService);
        endpoints.start(7070);
    }

    @Override
    public void onDisable() {
        endpoints.close();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("Hello, " + event.getPlayer().getName() + "!"));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Bukkit.getLogger().info("Chat event sent: " + event.message());
    }
}

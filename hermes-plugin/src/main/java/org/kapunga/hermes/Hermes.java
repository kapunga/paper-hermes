package org.kapunga.hermes;

import io.javalin.Javalin;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.kapunga.hermes.api.ApiBuilder;
import org.kapunga.hermes.link.HermesLinkCommand;
import org.kapunga.hermes.link.PlayerLinkService;

import java.util.Objects;

public class Hermes extends JavaPlugin {
    PlayerLinkService playerLinkService;
    LuckPerms luckPermsApi;
    Javalin endpoints;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        luckPermsApi = Objects.requireNonNull(
                Bukkit.getServicesManager().getRegistration(LuckPerms.class)).getProvider();

        playerLinkService = new PlayerLinkService(getConfig(), getDataFolder(), luckPermsApi);

        Objects.requireNonNull(getCommand("hermeslink")).setExecutor(new HermesLinkCommand(playerLinkService));
        Bukkit.getLogger().info("Hermes Plugin Started");

        int serverPort = getConfig().getInt("server-port");
        endpoints = ApiBuilder.create(playerLinkService);
        endpoints.start("0.0.0.0", serverPort);

    }

    @Override
    public void onDisable() {
        endpoints.close();
    }
}

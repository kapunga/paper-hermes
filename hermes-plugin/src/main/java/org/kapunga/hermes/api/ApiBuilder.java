package org.kapunga.hermes.api;

import io.javalin.Javalin;
import io.javalin.json.JavalinGson;
import org.bukkit.Bukkit;
import org.kapunga.hermes.link.PlayerLinkRequest;
import org.kapunga.hermes.link.PlayerLinkService;


import java.util.concurrent.TimeUnit;

public class ApiBuilder {
    public static Javalin create(PlayerLinkService playerLinkService) {
        Bukkit.getLogger().info("Creating web service");

        return Javalin.create(config -> config.jsonMapper(new JavalinGson()))
                .get("/", ctx -> ctx.result("Hello World"))
                .get("/groups", ctx -> ctx.json(playerLinkService.listGroups()))
                .get("/players", ctx -> ctx.json(playerLinkService.listPlayers()))
                .post("/link", ctx -> {
                    PlayerLinkRequest body = ctx.bodyValidator(PlayerLinkRequest.class).get();

                    ctx.future(() -> playerLinkService.requestLinkPlayer(body)
                            .completeOnTimeout(false, 30, TimeUnit.SECONDS));
                })
                .start(7070);
    }
}

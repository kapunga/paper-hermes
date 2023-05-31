package org.kapunga.hermes.link;

import java.util.concurrent.CompletableFuture;

public record PendingLinkRequest(PlayerLinkRequest req, CompletableFuture<Boolean> future) { }

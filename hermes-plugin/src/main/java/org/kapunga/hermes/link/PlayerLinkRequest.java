package org.kapunga.hermes.link;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public record PlayerLinkRequest(
        UUID id,
        @SerializedName("slack_name") String slackName,
        @SerializedName("slack_id") String slackId,
        String group) { }

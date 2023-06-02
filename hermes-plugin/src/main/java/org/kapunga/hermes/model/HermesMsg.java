package org.kapunga.hermes.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;

public class HermesMsg {
    public static Component linkRequest(String slackName, String group) {
        TextComponent linkMsg = Component.text("Link to Slack User '" +
                slackName + "' and group '" + group + "'?");

        TextComponent accept = Component.text("[Accept]")
                .color(TextColor.color(0x00ff00))
                .clickEvent(ClickEvent.runCommand("/hermeslink accept"));

        TextComponent reject = Component.text("[Reject]")
                .color(TextColor.color(0xff0000))
                .clickEvent(ClickEvent.runCommand("/hermeslink deny"));

        return Component.join(JoinConfiguration.separator(Component.space()), linkMsg, accept, reject);
    }
}

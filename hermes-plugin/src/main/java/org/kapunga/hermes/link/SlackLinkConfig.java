package org.kapunga.hermes.link;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SlackLinkConfig {
    public static final String SLACK_ID_SECTION = "slack-user-ids";
    private Logger logger = Bukkit.getLogger();
    private File playerLinkFile;
    private YamlConfiguration playerLinkConf;

    public SlackLinkConfig(File pluginData) {
        if (!pluginData.exists()) {
            pluginData.mkdirs();
        }
        playerLinkFile = new File(pluginData, "playerLink.yml");
        playerLinkConf = YamlConfiguration.loadConfiguration(playerLinkFile);

        if (playerLinkConf.getConfigurationSection(SLACK_ID_SECTION) == null) {
            playerLinkConf.createSection(SLACK_ID_SECTION);
        }
    }

    public boolean isPlayerLinked(UUID playerId) {
        return configSection().getString(playerId.toString()) != null;
    }

    public @Nullable String getPlayerSlackUserId(UUID playerId) {
        return configSection().getString(playerId.toString());
    }

    public boolean setPlayerSlackUserId(UUID playerId, String slackId) {
        configSection().set(playerId.toString(), slackId);
        try {
            playerLinkConf.save(playerLinkFile);
            return true;
        } catch (IOException e) {
            logger.severe("Error saving player Slack link file.");
            return false;
        }
    }

    public Set<UUID> getLinkedPlayers() {
        return configSection()
                .getKeys(false)
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    private ConfigurationSection configSection() {
        ConfigurationSection cs = playerLinkConf.getConfigurationSection(SLACK_ID_SECTION);
        if (cs == null) {
            cs = playerLinkConf.createSection(SLACK_ID_SECTION);
        }

        return cs;
    }
}

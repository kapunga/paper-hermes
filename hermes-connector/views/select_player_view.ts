import { MinecraftPlayer } from "../functions/link_mc_account_function.ts";

export const renderSelectPlayerView = (
  players: MinecraftPlayer[],
  contextArray: string[],
) => {
  const view = {
    "title": {
      "type": "plain_text",
      "text": "Slack/Minecraft Linker",
      "emoji": true,
    },
    "submit": {
      "type": "plain_text",
      "text": "Submit",
      "emoji": true,
    },
    "type": "modal",
    "callback_id": "select_player_view",
    "close": {
      "type": "plain_text",
      "text": "Cancel",
      "emoji": true,
    },
    "blocks": [
      {
        "type": "header",
        "text": {
          "type": "plain_text",
          "text": "Link Your Minecraft Account",
          "emoji": true,
        },
      },
      {
        "type": "input",
        "block_id": "player_block",
        "element": {
          "type": "static_select",
          "placeholder": {
            "type": "plain_text",
            "text": "Select a player",
            "emoji": true,
          },
          "options": playerElements(players),
          "action_id": "select_player",
        },
        "label": {
          "type": "plain_text",
          "text": "Which Player Is You?",
          "emoji": true,
        },
      },
      {
        "type": "input",
        "block_id": "context_block",
        "element": {
          "type": "static_select",
          "placeholder": {
            "type": "plain_text",
            "text": "Select a context",
            "emoji": true,
          },
          "options": contextElements(contextArray),
          "action_id": "select_context",
        },
        "label": {
          "type": "plain_text",
          "text": "Where do you know Thor from?",
          "emoji": true,
        },
      },
    ],
  };

  return view;
};

const playerElements = (players: MinecraftPlayer[]) =>
  players.map((player) => makePlayerElement(player));

const makePlayerElement = (player: MinecraftPlayer) => {
  return {
    "text": {
      "type": "plain_text",
      "text": `${player.name}`,
      "emoji": true,
    },
    "value": `${player.id}`,
  };
};

const contextElements = (contexts: string[]) =>
  contexts.map((ctx) => makeContextElement(ctx));

const makeContextElement = (ctx: string) => {
  return {
    "text": {
      "type": "plain_text",
      "text": `${ctx}`,
      "emoji": true,
    },
    "value": `${ctx.toLowerCase()}`,
  };
};

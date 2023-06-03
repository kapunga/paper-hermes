import {
  DefineFunction,
  Schema,
  SlackAPI,
  SlackFunction,
} from "deno-slack-sdk/mod.ts";
import { SlackAPIClient } from "deno-slack-sdk/types.ts";
import { renderSelectPlayerView } from "../views/select_player_view.ts";
import { McConnectResponse, McHermesClient } from "../lib/mc_hermes_client.ts";

export const LinkMcAccountFunction = DefineFunction({
  callback_id: "link_mc_account_function",
  title: "Link Minecraft Account",
  source_file: "functions/link_mc_account_function.ts",
  input_parameters: {
    properties: {
      channel_id: {
        type: Schema.slack.types.channel_id,
      },
      user_id: {
        type: Schema.slack.types.user_id,
      },
      interactivity: {
        type: Schema.slack.types.interactivity,
      },
      mc_hermes_host: {
        type: Schema.types.string,
      },
    },
    required: ["channel_id", "user_id", "interactivity", "mc_hermes_host"],
  },
  output_parameters: {
    properties: {},
    required: [],
  },
});

export default SlackFunction(
  LinkMcAccountFunction,
  async ({ inputs, token }) => {
    const mcHermesClient: McHermesClient = new McHermesClient(
      inputs.mc_hermes_host,
    );
    const slackClient: SlackAPIClient = SlackAPI(token);

    const players = await mcHermesClient.listUnlinkedPlayers();
    const groups = await mcHermesClient.listGroups();

    if (players.length === 0) {
      slackClient.chat.postEphemeral({
        channel: inputs.channel_id,
        user: inputs.user_id,
        text:
          "There are no unlinked players logged into the server. Please ensure you are logged in. If you are, it's possible you have already linked your accounts.",
      });
      return { outputs: {} };
    } else {
      const result = await slackClient.views.open({
        trigger_id: inputs.interactivity.interactivity_pointer,
        view: renderSelectPlayerView(players, groups),
      });

      if (!result.ok) throw new Error(result.error);

      return { completed: false };
    }
  },
).addViewSubmissionHandler(
  "select_player_view",
  async ({ inputs, body, view, token }) => {
    const mcHermesClient: McHermesClient = new McHermesClient(
      inputs.mc_hermes_host,
    );
    const slackClient: SlackAPIClient = SlackAPI(token);

    const player =
      view.state.values.player_block.select_player.selected_option.value;
    const group =
      view.state.values.context_block.select_context.selected_option.value;

    const userResp = await slackClient.users.info({ user: inputs.user_id });
    const name = userResp.user.real_name;

    const result = await mcHermesClient.sendLinkRequest({
      id: player,
      slack_name: name,
      slack_id: inputs.user_id,
      group: group,
    });

    let resultMsg;

    switch (result) {
      case McConnectResponse.Success:
        resultMsg =
          ":tada: Your Slack and Minecraft accounts have been successfully linked!";
        break;
      case McConnectResponse.ConnectDenied:
        resultMsg =
          ":no_entry: Minecraft player rejected link request, make sure you are linking to your own player.";
        break;
      case McConnectResponse.AlreadyLinked:
        resultMsg =
          ":warning: Your account is already linked. If you think this is a mistake, please contact an admin.";
        break;
      case McConnectResponse.Timeout:
        resultMsg =
          ":hourglass: Attempt to link your accounts timed out, please try again later.";
        break;
      case McConnectResponse.ServerError:
        resultMsg = "Server error, please try again later.";
        break;
    }

    slackClient.chat.postEphemeral({
      channel: inputs.channel_id,
      user: inputs.user_id,
      text: resultMsg,
    });

    await slackClient.functions.completeSuccess({
      function_execution_id: body.function_data.execution_id,
      outputs: {},
    });
  },
);

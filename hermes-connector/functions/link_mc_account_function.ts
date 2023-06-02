import {
  DefineFunction,
  Schema,
  SlackAPI,
  SlackFunction,
} from "deno-slack-sdk/mod.ts";
import { SlackAPIClient } from "deno-slack-sdk/types.ts";
import { renderSelectPlayerView } from "../views/select_player_view.ts";

export type MinecraftPlayer = {
  name: string;
  id: string;
};

export const LinkMcAccountFunction = DefineFunction({
  callback_id: "link_mc_account_function",
  title: "Link Minecraft Account",
  source_file: "functions/link_mc_account_function.ts",
  input_parameters: {
    properties: {
      user_id: {
        type: Schema.slack.types.user_id,
      },
      interactivity: {
        type: Schema.slack.types.interactivity,
      },
    },
    required: ["user_id", "interactivity"],
  },
  output_parameters: {
    properties: {},
    required: [],
  },
});

export default SlackFunction(
  LinkMcAccountFunction,
  async ({ inputs, token }) => {
    const client: SlackAPIClient = SlackAPI(token);
    const playersResponse = await fetch("http://localhost:7070/players");
    const groupsResponse = await fetch("http://localhost:7070/groups");

    if (playersResponse.status !== 200) {
      console.log("Non 2xx response for players: " + playersResponse.status);
      return { outputs: {} };
    } else if (groupsResponse.status !== 200) {
      console.log("Non 2xx response for groups: " + groupsResponse.status);
      return { outputs: {} };
    } else {
      const players: MinecraftPlayer[] = await playersResponse
        .json() as MinecraftPlayer[];
      const groups: string[] = await groupsResponse.json() as string[];

      console.log(`Player response: ${JSON.stringify(players)}`);
      console.log(`Groups response: ${JSON.stringify(groups)}`);

      console.log(typeof groups);

      const result = await client.views.open({
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
    const client: SlackAPIClient = SlackAPI(token);

    const player =
      view.state.values.player_block.select_player.selected_option.value;
    const group =
      view.state.values.context_block.select_context.selected_option.value;

    console.log(`Player: ${player} Context: ${group}`);

    const userResp = await client.users.info({ user: inputs.user_id });

    const name = userResp.user.real_name;

    const payload = {
      id: player,
      slack_name: name,
      slack_id: inputs.user_id,
      group: group,
    };

    const resp = await fetch("http://localhost:7070/link/", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    if (resp.status === 200) {
      console.log("Success!");
    } else {
      console.log("Failure!");
    }

    await client.functions.completeSuccess({
      function_execution_id: body.function_data.execution_id,
      outputs: {},
    });
  },
);

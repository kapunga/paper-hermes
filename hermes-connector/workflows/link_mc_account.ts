import { DefineWorkflow, Schema } from "deno-slack-sdk/mod.ts";
import { LinkMcAccountFunction } from "../functions/link_mc_account_function.ts";

export const LinkMcAccountsWorkflow = DefineWorkflow({
  callback_id: "link_mc_accounts_workflow",
  title: "Link Minecraft Account",
  description: "Link Minecraft Account",
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
});

LinkMcAccountsWorkflow.addStep(LinkMcAccountFunction, {
  channel_id: LinkMcAccountsWorkflow.inputs.channel_id,
  user_id: LinkMcAccountsWorkflow.inputs.user_id,
  interactivity: LinkMcAccountsWorkflow.inputs.interactivity,
  mc_hermes_host: LinkMcAccountsWorkflow.inputs.mc_hermes_host,
});

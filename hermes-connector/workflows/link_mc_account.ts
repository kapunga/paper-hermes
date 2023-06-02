import { DefineWorkflow, Schema } from "deno-slack-sdk/mod.ts";
import { LinkMcAccountFunction } from "../functions/link_mc_account_function.ts";

export const LinkMcAccountsWorkflow = DefineWorkflow({
  callback_id: "link_mc_accounts_workflow",
  title: "Link Minecraft Account",
  description: "Link Minecraft Account",
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
});

LinkMcAccountsWorkflow.addStep(LinkMcAccountFunction, {
  user_id: LinkMcAccountsWorkflow.inputs.user_id,
  interactivity: LinkMcAccountsWorkflow.inputs.interactivity,
});

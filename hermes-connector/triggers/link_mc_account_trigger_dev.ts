import { Trigger } from "deno-slack-api/types.ts";
import { LinkMcAccountsWorkflow } from "../workflows/link_mc_account.ts";

const linkMcAccountsTrigger: Trigger<
  typeof LinkMcAccountsWorkflow.definition
> = {
  type: "shortcut",
  name: "Link Minecraft Account (Dev)",
  description: "Link Minecraft Account",
  workflow: "#/workflows/link_mc_accounts_workflow",
  inputs: {
    channel_id: {
      value: "{{data.channel_id}}",
    },
    user_id: {
      value: "{{data.user_id}}",
    },
    interactivity: {
      value: "{{data.interactivity}}",
    },
    mc_hermes_host: {
      value: "http://localhost:7070",
    },
  },
};

export default linkMcAccountsTrigger;

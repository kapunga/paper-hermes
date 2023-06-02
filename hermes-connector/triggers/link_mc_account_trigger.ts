import { Trigger } from "deno-slack-api/types.ts";
import { LinkMcAccountsWorkflow } from "../workflows/link_mc_account.ts";

const linkMcAccountsTrigger: Trigger<
  typeof LinkMcAccountsWorkflow.definition
> = {
  type: "shortcut",
  name: "Link Minecraft Account",
  description: "Link Minecraft Account",
  workflow: "#/workflows/link_mc_accounts_workflow",
  inputs: {
    user_id: {
      value: "{{data.user_id}}",
    },
    interactivity: {
      value: "{{data.interactivity}}",
    },
  },
};

export default linkMcAccountsTrigger;

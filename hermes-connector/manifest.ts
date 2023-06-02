import { Manifest } from "deno-slack-sdk/mod.ts";
import { LinkMcAccountFunction } from "./functions/link_mc_account_function.ts";
import { LinkMcAccountsWorkflow } from "./workflows/link_mc_account.ts";

/**
 * The app manifest contains the app's configuration. This
 * file defines attributes like app name and description.
 * https://api.slack.com/future/manifest
 */
export default Manifest({
  name: "hermes-connector",
  description: "A blank template for building Slack apps with Deno",
  icon: "assets/default_new_app_icon.png",
  functions: [LinkMcAccountFunction],
  workflows: [LinkMcAccountsWorkflow],
  outgoingDomains: ["kapunga.org", "localhost"],
  botScopes: ["commands", "chat:write", "chat:write.public", "users:read"],
});

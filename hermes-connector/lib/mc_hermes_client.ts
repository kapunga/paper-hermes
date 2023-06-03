export interface MinecraftPlayer {
  name: string;
  id: string;
}

export interface McConnectRequest {
  id: string;
  slack_name: string;
  slack_id: string;
  group: string;
}

export enum McConnectResponse {
  Success,
  ConnectDenied,
  AlreadyLinked,
  Timeout,
  ServerError,
}

export class McHermesClient {
  host: string;

  constructor(host: string) {
    this.host = host;
  }

  async listGroups(): Promise<string[]> {
    const groupsResponse = await fetch(`${this.host}/groups`);

    if (groupsResponse.status !== 200) {
      throw new Error("Failed while fetching groups.");
    } else {
      return await groupsResponse.json() as string[];
    }
  }

  async listUnlinkedPlayers(): Promise<MinecraftPlayer[]> {
    const playersResponse = await fetch(`${this.host}/players`);

    if (playersResponse.status !== 200) {
      throw new Error("Failed while fetching unlinked player.");
    } else {
      return await playersResponse.json() as MinecraftPlayer[];
    }
  }

  async sendLinkRequest(request: McConnectRequest): Promise<McConnectResponse> {
    const resp = await fetch(`${this.host}/link/`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });

    switch (resp.status) {
      case 200:
        return McConnectResponse.Success;
      case 403:
        return McConnectResponse.ConnectDenied;
      case 408:
        return McConnectResponse.Timeout;
      case 409:
        return McConnectResponse.AlreadyLinked;
      default:
        return McConnectResponse.ServerError;
    }
  }
}

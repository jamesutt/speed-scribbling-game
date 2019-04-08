package game.models.messages;

import game.models.Player;

public class ClientDisconnectMessage extends Message {
    Player disconnectedPlayer;

    public ClientDisconnectMessage(Player disconnectedPlayer) {
        super(MessageType.CLIENT_DISCONNECT);
        this.disconnectedPlayer = disconnectedPlayer;
    }

    public Player getDisconnectedPlayer() {
        return disconnectedPlayer;
    }
}
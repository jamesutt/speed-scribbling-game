package game.models.messages;

import game.models.Player;

public class ConfirmConnectionMessage extends Message {
    private Player player;

    public ConfirmConnectionMessage(Player player) {
        super(MessageType.CONFIRM_CONNECTION);

        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}

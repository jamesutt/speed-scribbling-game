package game.models;

import java.io.Serializable;

public class Message implements Serializable {
    private MessageType type;

    public Message(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }
}

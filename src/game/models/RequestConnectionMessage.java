package game.models;

import java.io.Serializable;

public class RequestConnectionMessage extends Message {
    private String name;
    private String ip;

    public RequestConnectionMessage(String name, String ip) {
        super(MessageType.REQUEST_CONNECTION);

        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }
}

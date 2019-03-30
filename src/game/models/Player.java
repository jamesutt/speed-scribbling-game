package game.models;

import java.awt.*;
import java.io.Serializable;

public class Player implements Serializable {
    private int id;
    private String name;
    private String ip;
    private Color color;

    public Player(int id, String name, String ip, Color color) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

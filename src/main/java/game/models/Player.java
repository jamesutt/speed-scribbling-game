package game.models;

import javafx.scene.paint.Color;

import java.io.Serializable;

public class Player implements Serializable {
    private int id;
    private String name;
    private String ip;
    private String color;

    public Player(int id, String name, String ip, String color) {
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

    public Color getColor() {
        return Color.web(color);
    }

    public String getColorString() {
        return color;
    }

    public String getIp() {
        return ip;
    }
}

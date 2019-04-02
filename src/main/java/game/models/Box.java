package game.models;

import javafx.scene.paint.Color;

import java.io.Serializable;

public class Box implements Serializable {
    private int ownerId = -1;
    private BoxStatus status = BoxStatus.FREE;
    private String color = "#ffffff";

    public void reset() {
        ownerId = -1;
        status = BoxStatus.FREE;
        color = "#ffffff";
    }

    public void reserve(int ownerId, String color) {
        this.ownerId = ownerId;
        this.status = BoxStatus.RESERVED;
        this.color = color;
    }

    public void fill() {
        this.status = BoxStatus.FILLED;
    }

    public BoxStatus getStatus() {
        return status;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Color getColor() {
        return Color.web(color);
    }
}

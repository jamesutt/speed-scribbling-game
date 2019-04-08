package game.models;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Box> boxes = new ArrayList<>();

    private double minPercentage;
    private double lineWidth;
    private int numRows;

    public GameState(int numRows, double minPercentage, double lineWidth) {
        this.minPercentage = minPercentage;
        this.lineWidth = lineWidth;
        this.numRows = numRows;
        for (int i = 0; i < numRows * numRows; i++) {
            boxes.add(new Box());
        }
    }

    public void setBoxes(ArrayList<Box> boxes) {
        this.boxes = boxes;
    }

    public void resetBox(int row, int column) {
        int index = toIndex(row, column);
        boxes.get(index).reset();
    }

    public void reserveBox(int row, int column, int ownerId, String color) {
        int index = toIndex(row, column);
        boxes.get(index).reserve(ownerId, color);
    }

    public void fillBox(int row, int column, int ownerId, String color) {
        int index = toIndex(row, column);
        boxes.get(index).reserve(ownerId, color);
        boxes.get(index).fill();
    }

    public Box getBox(int row, int column) {
        int index = toIndex(row, column);
        return boxes.get(index);
    }

    public ArrayList<Box> getBoxes() {
        return boxes;
    }

    private int toIndex(int row, int column) {
        return row * numRows + column;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public int getNumRows() {
        return numRows;
    }
}

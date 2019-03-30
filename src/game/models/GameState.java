package game.models;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Box> boxes = new ArrayList<>();
    private int size;
    private boolean gameStart = false;

    public GameState(int size) {
        this.size = size;
        for (int i = 0; i < size; i++) {
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

    public void drawBox(int row, int column, int ownerId) {
        int index = toIndex(row, column);
        boxes.get(index).draw(ownerId);
    }

    public void completeBox(int row, int column) {
        int index = toIndex(row, column);
        boxes.get(index).complete();
    }

    public Box getBox(int row, int column) {
        int index = toIndex(row, column);
        return boxes.get(index);
    }

    private int toIndex(int row, int column) {
        return row * size + column;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public boolean isGameStart() {
        return gameStart;
    }

    public void setGameStart(boolean gameStart) {
        this.gameStart = gameStart;
    }

}

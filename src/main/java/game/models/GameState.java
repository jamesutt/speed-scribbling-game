package game.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameState implements Serializable {
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Box> boxes = new ArrayList<>();

    private double minPercentage;
    private double lineWidth;
    private int numRows;
    private String winnerText;

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

        // Check for winners
        if (areAllBoxesFilled()) {
            ArrayList<Player> winners = getWinners();

            if (winners.size() == 1) {
                winnerText = "The winner is " + winners.get(0).getName();
            } else if (winners.size() > 1) {
                String winnerNames = winners.get(0).getName();
                for (int i = 1; i < winners.size(); i++) {
                    winnerNames += ", " + winners.get(i).getName();
                }

                winnerText = "The winners are " + winnerNames;
            }
        }
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

    public boolean areAllBoxesFilled() {
        for (int i = 0; i < boxes.size(); i++) {
            if (boxes.get(i).getStatus() != BoxStatus.FILLED) {
                return false;
            }
        }

        return true;
    }

    public ArrayList<Player> getWinners() {
        // key = player id
        // value = number of boxes the player owns
        Map<Integer, Integer> boxCount = new HashMap<Integer, Integer>();

        for (int i = 0; i < boxes.size(); i++) {
            Box box = boxes.get(i);
            if (box.getStatus() == BoxStatus.FILLED) {
                int ownerId = box.getOwnerId();
                int count = boxCount.getOrDefault(ownerId, 0);
                boxCount.put(ownerId, count + 1);
            }
        }

        // Find highest number of boxes owned by a player
        int maxBoxCount = 0;
        for (Map.Entry<Integer, Integer> entry : boxCount.entrySet()) {
            if (entry.getValue() > maxBoxCount) {
                maxBoxCount = entry.getValue();
            }
        }

        // Find players with highest number of boxes
        ArrayList<Player> winners = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : boxCount.entrySet()) {
            int count = entry.getValue();
            int playerId = entry.getKey(); // player id with most boxes

            if (count == maxBoxCount) {
                Player player = getPlayerFromPlayerId(playerId);
                winners.add(player);
            }
        }

        return winners;
    }

    private Player getPlayerFromPlayerId(int playerId) {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.getId() == playerId) {
                return player;
            }
        }

        return null;
    }

    public String getWinnerText() {
        return winnerText;
    }
}

package game.scenes;

import game.Main;
import game.models.Box;
import game.models.BoxStatus;
import game.models.GameState;
import game.views.BoxView;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class GameScene extends Scene {

    ArrayList<BoxView> boxViews = new ArrayList<>();

    public GameScene(int numRows) {
        super(new VBox());

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setStyle("-fx-padding: 100; -fx-alignment: center;");

        for (int row = 0; row < numRows; row++) {
            for (int column = 0; column < numRows; column++) {
                BoxView boxView = new BoxView(row, column);
                boxViews.add(boxView);
                grid.add(boxView, row, column);
            }
        }

        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(grid);

        this.setRoot(layout);
    }

    // Not used. Might be useful in the future.
    private static int getSceneSize(int rows) {
        return rows * 52 + (rows - 1) * 5 + 10 * 2 + 100;
    }

    public void updateUI(GameState state) {
        Platform.runLater(() -> {
            ArrayList<Box> boxes = state.getBoxes();
            for (int i = 0; i < boxes.size(); i++) {
                Box box = boxes.get(i);
                BoxStatus boxStatus = box.getStatus();
                int boxOwnerId = box.getOwnerId();
                Color boxColor = box.getColor();
                int currentPlayerId = Main.getCurrentPlayer().getId();

                if (boxStatus.equals(BoxStatus.FREE)) {
                    boxViews.get(i).reset();
                }

                if (boxStatus.equals(BoxStatus.RESERVED) && boxOwnerId != currentPlayerId) {
                    boxViews.get(i).reserve(boxColor);
                }

                if (boxStatus.equals(BoxStatus.FILLED) && boxOwnerId != currentPlayerId) {
                    boxViews.get(i).forceFill(boxColor);
                }
            }
        });
    }

}

package game.scenes;

import game.Main;
import game.views.StyledBoxView;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class GameScene extends Scene {

    public GameScene(int size) {
        super(new VBox(), 500, 500);

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setStyle("-fx-padding: 10;");

        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                StyledBoxView boxView = new StyledBoxView(50, 50, 10, Color.RED, 50);
                grid.add(boxView, row, column);
            }
        }

        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(grid);

        this.setRoot(layout);
    }
}

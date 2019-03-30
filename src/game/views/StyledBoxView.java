package game.views;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;

public class StyledBoxView extends StackPane {
    public BoxView box;

    public StyledBoxView(double width, double height, double lineWidth, Paint color, double minPercentage) {
        super();

        box = new BoxView(width, height, lineWidth, color, minPercentage);

        this.getChildren().add(box);
        this.setStyle("-fx-border-color: black; -fx-border-width: 1;");
    }
}

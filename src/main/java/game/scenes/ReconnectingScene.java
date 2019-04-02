package game.scenes;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ReconnectingScene extends Scene {

    public ReconnectingScene() {
        super(new VBox(), 500, 500);

        Label reconnectingLabel = new Label("Reconnecting...");

        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(reconnectingLabel);

        this.setRoot(layout);
    }

}


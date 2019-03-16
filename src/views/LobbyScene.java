package views;

import controllers.GameController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LobbyScene extends Scene {

    GameController parent;

    public LobbyScene(GameController parent) {
        super(new VBox());

        this.parent = parent;
        setupUI();
    }

    void setupUI() {
        Label player1 = new Label("Player 1");
        Label player2 = new Label("Player 2");
        Button readyButton = new Button("Ready");

        VBox layout = new VBox(10, player1, player2, readyButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 10");

        this.setRoot(layout);
    }
}

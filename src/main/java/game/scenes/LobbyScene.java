package game.scenes;

import game.Main;
import game.models.GameState;
import game.models.Player;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class LobbyScene extends Scene {

    Label playerLabel1 = new Label("");
    Label playerLabel2 = new Label("");
    Label playerLabel3 = new Label("");
    Label playerLabel4 = new Label("");

    public LobbyScene() {
        super(new VBox(), 500, 500);

        Button startButton = new Button("Start");
        startButton.setOnAction(e -> Main.onStartClicked());

        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(playerLabel1, playerLabel2, playerLabel3, playerLabel4, startButton);

        this.setRoot(layout);
    }

    public void updateUI(GameState state) {
        Platform.runLater(() -> {
            ArrayList<Player> players = state.getPlayers();

            playerLabel1.setText(players.size() > 0 ? players.get(0).getName() : "");
            playerLabel2.setText(players.size() > 1 ? players.get(1).getName() : "");
            playerLabel3.setText(players.size() > 2 ? players.get(2).getName() : "");
            playerLabel4.setText(players.size() > 3 ? players.get(3).getName() : "");
        });
    }
}

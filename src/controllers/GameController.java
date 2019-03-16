package controllers;

import javafx.application.Application;
import javafx.stage.Stage;
import views.GameSetupScene;
import views.LobbyScene;

public class GameController extends Application {

    Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        GameSetupScene gameSetupScene = new GameSetupScene(this);
        stage.setScene(gameSetupScene);
        stage.setTitle("Game Setup");

        stage.show();
    }

    public void handleCreateButtonClicked(String msg) {
        LobbyScene lobbyScene = new LobbyScene(this);
        stage.setScene(lobbyScene);
        stage.setTitle("Lobby");
    }

    public void handleJoinButtonClicked() {

    }

    public static void main(String[] args) {
        launch(args);
    }
}

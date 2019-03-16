package views;

import controllers.GameController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GameSetupScene extends Scene {

    GameController parent;

    public GameSetupScene(GameController parent) {
        super(new VBox());

        this.parent = parent;
        setupUI();
    }

    void setupUI() {
        TextField configTextField = new TextField();

        Button createButton = new Button("Create");
        createButton.setOnAction(e -> parent.handleCreateButtonClicked("Create Button Click"));

        Label separatorLabel = new Label("or");

        Button joinButton = new Button("Join");

        VBox layout = new VBox(10, configTextField, createButton, separatorLabel, joinButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 10");

        this.setRoot(layout);
    }
}

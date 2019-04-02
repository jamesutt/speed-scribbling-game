package game.scenes;

import game.Main;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class MenuScene extends Scene {

    public MenuScene() {
        super(new VBox(), 500, 500);

        Button serverButton = new Button("Server");
        serverButton.setOnAction(e -> Main.onServerClicked());
        Button clientButton = new Button("Client");
        clientButton.setOnAction(e -> Main.onClientClicked());

        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(serverButton, clientButton);

        this.setRoot(layout);
    }

}

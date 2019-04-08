package game.scenes;

import game.Main;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class MenuScene extends Scene {

    public MenuScene() {
        super(new VBox(), 350, 500);


        /**
         * Create client tab
         */

        Label clientNameLabel = new Label("Name");
        clientNameLabel.setLayoutX(37);
        clientNameLabel.setLayoutY(50);

        TextField clientNameTextField = new TextField();
        clientNameTextField.setLayoutX(115);
        clientNameTextField.setLayoutY(45);

        Label serverIpLabel = new Label("Server's IP");
        serverIpLabel.setLayoutX(34);
        serverIpLabel.setLayoutY(112);

        TextField serverIpTextField = new TextField();
        serverIpTextField.setLayoutX(115);
        serverIpTextField.setLayoutY(106);

        Button connectButton = new Button("Connect");
        connectButton.setLayoutX(134);
        connectButton.setLayoutY(163);

        Label clientYourIpLabel = new Label("Your IP is " + Main.MY_IP_ADDRESS);
        clientYourIpLabel.setLayoutX(95);
        clientYourIpLabel.setLayoutY(210);

        AnchorPane clientTabContent = new AnchorPane();
        clientTabContent.getChildren().addAll(clientNameLabel, clientNameTextField, serverIpLabel, serverIpTextField, connectButton, clientYourIpLabel);

        Tab clientTab = new Tab();
        clientTab.setClosable(false);
        clientTab.setText("Client");
        clientTab.setContent(clientTabContent);

        connectButton.setOnAction(e -> {
            Main.onClientClicked(clientNameTextField.getText(), serverIpTextField.getText());
        });

        /**
         * Create server tab
         */

        Label serverNameLabel = new Label("Name");
        serverNameLabel.setLayoutX(34);
        serverNameLabel.setLayoutY(50);

        TextField serverNameTextField = new TextField();
        serverNameTextField.setLayoutX(115);
        serverNameTextField.setLayoutY(45);

        Label numPlayersLabel = new Label("Total players");
        numPlayersLabel.setLayoutX(34);
        numPlayersLabel.setLayoutY(109);

        ChoiceBox numberPlayersChoiceBox = new ChoiceBox();
        numberPlayersChoiceBox.setLayoutX(160);
        numberPlayersChoiceBox.setLayoutY(109 - 5);
        numberPlayersChoiceBox.setItems(FXCollections.observableArrayList(2, 3, 4));
        numberPlayersChoiceBox.getSelectionModel().select(2);

        Label minPercentageLabel = new Label("Min. Percentage");
        minPercentageLabel.setLayoutX(34);
        minPercentageLabel.setLayoutY(150);

        ChoiceBox minPercentageChoiceBox = new ChoiceBox();
        minPercentageChoiceBox.setLayoutX(160);
        minPercentageChoiceBox.setLayoutY(150 - 5);
        minPercentageChoiceBox.setItems(FXCollections.observableArrayList(25, 50, 75));
        minPercentageChoiceBox.getSelectionModel().select(0);

        Label lineWidthLabel = new Label("Brush Size");
        lineWidthLabel.setLayoutX(34);
        lineWidthLabel.setLayoutY(200);

        ChoiceBox lineWidthChoiceBox = new ChoiceBox();
        lineWidthChoiceBox.setLayoutX(160);
        lineWidthChoiceBox.setLayoutY(200 - 5);
        lineWidthChoiceBox.setItems(FXCollections.observableArrayList(2, 5, 10));
        lineWidthChoiceBox.getSelectionModel().select(2);

        Label rowsLabel = new Label("Rows");
        rowsLabel.setLayoutX(34);
        rowsLabel.setLayoutY(250);

        ChoiceBox rowsChoiceBox = new ChoiceBox();
        rowsChoiceBox.setLayoutX(160);
        rowsChoiceBox.setLayoutY(250 - 5);
        rowsChoiceBox.setItems(FXCollections.observableArrayList(2, 4, 8, 12));
        rowsChoiceBox.getSelectionModel().select(1);

        Button createButton = new Button("Create");
        createButton.setLayoutX(134);
        createButton.setLayoutY(300);

        Label serverYourIpLabel = new Label("Your IP is " + Main.MY_IP_ADDRESS);
        serverYourIpLabel.setLayoutX(95);
        serverYourIpLabel.setLayoutY(350);

        AnchorPane serverTabContent = new AnchorPane();
        serverTabContent.getChildren().addAll(
                serverNameLabel,
                serverNameTextField,
                numPlayersLabel,
                numberPlayersChoiceBox,
                minPercentageLabel,
                minPercentageChoiceBox,
                lineWidthLabel,
                lineWidthChoiceBox,
                rowsLabel,
                rowsChoiceBox,
                createButton,
                serverYourIpLabel
        );

        Tab serverTab = new Tab();
        serverTab.setClosable(false);
        serverTab.setText("Server");
        serverTab.setContent(serverTabContent);

        createButton.setOnAction(e -> {
            Main.onServerClicked(
                    serverNameTextField.getText(),
                    (int) numberPlayersChoiceBox.getValue(),
                    (int) minPercentageChoiceBox.getValue(),
                    (int) lineWidthChoiceBox.getValue(),
                    (int) rowsChoiceBox.getValue()
            );
        });

        /**
         * Create tab pane and set as root
         */

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(clientTab, serverTab);

        this.setRoot(tabPane);
    }

}

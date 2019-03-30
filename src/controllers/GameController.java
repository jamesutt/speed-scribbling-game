package controllers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import services.Client;
import services.NetworkConnection;
import services.Server;
import views.GameSetupScene;
import views.LobbyScene;

enum Status {
    JOINING_LOBBY,
    LOBBY,
    IN_GAME
}

public class GameController extends Application {

    private Boolean isServer;
    private Status status;
    private Stage stage;
    private NetworkConnection connection;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        GameSetupScene gameSetupScene = new GameSetupScene(this);
        stage.setScene(gameSetupScene);
        stage.setTitle("Game Setup");

        stage.show();
    }

    public void handleCreateButtonClicked() {
        isServer = true;
        status = Status.LOBBY;
        connection = createServer();

        LobbyScene lobbyScene = new LobbyScene(this);
        stage.setScene(lobbyScene);
        stage.setTitle("Lobby - Server");
    }

    public void handleJoinButtonClicked() {
        isServer = false;
        status = Status.JOINING_LOBBY;
        connection = createClient();

        try {
            connection.send("JOIN_LOBBY");
        } catch (Exception e) {
            System.out.println("Can't send");
            e.printStackTrace();
        }


        LobbyScene lobbyScene = new LobbyScene(this);
        stage.setScene(lobbyScene);
        stage.setTitle("Lobby - Client");
    }

    private Server createServer() {
        Server server = new Server(8888, data -> {
            String[] messages = data.toString().split(" ");

            if (status.equals(Status.LOBBY) && messages[0].equals("JOIN_LOBBY")) {
                try {
                    connection.send("OK");
                } catch (Exception e) {
                    System.out.println("Can't send");
                    e.printStackTrace();
                }
            }
        });

        try {
            server.startConnection();
        } catch (Exception e) {
            System.out.println("Unable to start Server connection");
            e.printStackTrace();
        }

        return server;
    }

    private Client createClient() {
        Client client = new Client("127.0.0.1", 8888, data -> {
            String[] messages = data.toString().split(" ");

            if (status.equals(Status.JOINING_LOBBY) && messages[0].equals("OK")) {
                System.out.println("Client - joined lobby");
            }
        });

        try {
            client.startConnection();
        } catch (Exception e) {
            System.out.println("Unable to start Client connection");
            e.printStackTrace();
        }

        return client;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

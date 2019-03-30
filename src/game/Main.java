package game;

import game.models.*;
import game.networking.ClientListener;
import game.networking.ServerListener;
import game.scenes.GameScene;
import game.scenes.LobbyScene;
import game.scenes.MenuScene;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

enum SCENE {
    LOGIN,
    LOBBY,
    GAME,
}

public class Main extends Application {

    // Constants
    private static final int NUM_CLIENTS = 2;
    public static final int size = 4;
    public static final String HOSTNAME = "localhost";
    public static final int PORT = 8888;
    private static final List<Color> COLORS = Arrays.asList(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN);

    // Shared
    private static Stage stage;
    private static GameState state;
    private static boolean isServer;
    private static LobbyScene lobbyScene;
    private static GameScene gameScene;
    private static SCENE currentScene;
    private static Player currentPlayer;

    // Server
    private static HashSet<ObjectOutputStream> writers = new HashSet<>();
    private static int nextPlayerId = 0;

    // Client
    private static ObjectOutputStream clientWriter;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        state = new GameState(size);

        MenuScene menuScene = new MenuScene();

        currentScene = SCENE.LOGIN;
        stage.setScene(menuScene);
        stage.setTitle("Menu");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void onServerClicked() {
        isServer = true;
        lobbyScene = new LobbyScene();
        stage.setScene(lobbyScene);
        currentScene = SCENE.LOBBY;

        addHostAsPlayer();

        Thread wait = new Thread(() -> {
            try {
                int count = 0;
                ServerSocket server = new ServerSocket(PORT);

                while (count < NUM_CLIENTS) {
                    new ServerListener(server.accept()).start();
                    count++;
                }
            } catch (Exception e) {
                 e.printStackTrace();
            }
        });
        wait.setDaemon(true);
        wait.start();
    }

    public static void onClientClicked() {
        isServer = false;
        lobbyScene = new LobbyScene();
        stage.setScene(lobbyScene);
        currentScene = SCENE.LOBBY;

        new ClientListener().start();
    }

    private static synchronized void addHostAsPlayer() {
        String name = "THE HOST";
        String ip = "Host's IP Address";
        int id = nextPlayerId;
        Color color = COLORS.get(id);

        nextPlayerId++;

        Player player = new Player(id, name, ip, color);
        currentPlayer = player;
        state.addPlayer(player);

        lobbyScene.updateUI(Main.state);
        sendStateToClients();
    }

    private static synchronized void sendStateToClients() {
        UpdateStateMessage message = new UpdateStateMessage(state);
        sendMessageToClients(message);
    }

    private static synchronized void sendMessageToClients(Message message) {
        try {
            for (ObjectOutputStream writer: writers) {
                writer.writeObject(message);
                writer.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void onStartClicked() {
        if (isServer) {
            gameScene = new GameScene(size);
            currentScene = SCENE.GAME;
            stage.setScene(gameScene);

            StartGameMessage startGameMessage = new StartGameMessage();
            sendMessageToClients(startGameMessage);
        }
    }

    public static synchronized void onMessageReceivedFromClient(Message message, ObjectOutputStream writer) {
        switch (message.getType()) {
            case REQUEST_CONNECTION:
                RequestConnectionMessage requestConnectionMessage = (RequestConnectionMessage) message;
                String name = requestConnectionMessage.getName();
                String ip = requestConnectionMessage.getIp();
                int id = nextPlayerId;

                Color color = COLORS.get(id);

                Player player = new Player(id, name, ip, color);
                state.addPlayer(player);
                writers.add(writer);

                try {
                    ConfirmConnectionMessage confirmConnectionMessage = new ConfirmConnectionMessage(player);
                    writer.writeObject(confirmConnectionMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                lobbyScene.updateUI(Main.state);
                sendStateToClients();
                nextPlayerId++;
                break;
        }
    }


    // Client
    public static synchronized void setClientWriter(ObjectOutputStream writer) {
        clientWriter = writer;
    }

    public static synchronized void onMessageReceivedFromServer(Message message) {
        switch (message.getType()) {
            case CONFIRM_CONNECTION:
                ConfirmConnectionMessage confirmConnectionMessage = (ConfirmConnectionMessage) message;
                Player player = confirmConnectionMessage.getPlayer();
                currentPlayer = player;
                break;
            case START_GAME:
                gameScene = new GameScene(size);
                currentScene = SCENE.GAME;

                Platform.runLater(() -> {
                    stage.setScene(gameScene);
                });
                break;
            case UPDATE_STATE:
                UpdateStateMessage updateStateMessage = (UpdateStateMessage) message;
                Main.state = updateStateMessage.getState();

                if (currentScene.equals(SCENE.LOBBY)) {
                    lobbyScene.updateUI(Main.state);
                } else if (currentScene.equals(SCENE.GAME)) {
                    gameScene.updateUI(Main.state);
                }
                break;
        }
    }

    }
}

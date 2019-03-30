package game;

import game.models.GameState;
import game.models.Player;
import game.networking.ClientListener;
import game.networking.ServerListener;
import game.scenes.GameScene;
import game.scenes.LobbyScene;
import game.scenes.MenuScene;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;

public class Main extends Application {

    // Constants
    private static final int NUM_CLIENTS = 2;
    public static final int size = 4;
    public static final String HOSTNAME = "localhost";
    public static final int PORT = 8888;

    // Shared
    private static Stage stage;
    private static GameState state;
    private static boolean isServer;
    private static LobbyScene lobbyScene;
    private static GameScene gameScene;

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

        new ClientListener().start();
    }

    private static synchronized void addHostAsPlayer() {
        Player player = new Player();
        player.setName("THE HOST");
        player.setIpAddress("Host's IP Address");
        player.setId(nextPlayerId);
        nextPlayerId++;

        state.addPlayer(player);

        lobbyScene.updateUI(Main.state);
        updateClients();
    }

    public static synchronized void onPlayerConnected(Player player, ObjectOutputStream writer) {
        player.setId(nextPlayerId);
        nextPlayerId++;

        state.addPlayer(player);
        writers.add(writer);

        lobbyScene.updateUI(Main.state);
        updateClients();
    }

    private static synchronized void updateClients() {
        try {
            for (ObjectOutputStream writer: writers) {
                writer.writeObject(state);
                writer.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void onStartClicked() {
        state.setGameStart(true);
        gameScene = new GameScene(size);
        stage.setScene(gameScene);
    }


    // Client
    public static synchronized void onConnectedToServer(ObjectOutputStream writer) {
        clientWriter = writer;
    }

    public static synchronized void onGameStateUpdated(GameState state) {
        Main.state = state;
        lobbyScene.updateUI(Main.state);
    }
}

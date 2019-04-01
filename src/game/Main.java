package game;

import game.models.*;
import game.models.messages.*;
import game.networking.ClientListener;
import game.networking.ServerListener;
import game.scenes.GameScene;
import game.scenes.LobbyScene;
import game.scenes.MenuScene;
import game.scenes.ReconnectingScene;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Main extends Application {

    // Constants
    private static final int NUM_CLIENTS = 3;
    public static final int NUM_ROWS = 4;
    public static final String HOSTNAME = "localhost";
    public static final int PORT = 8888;
    private static final List<String> COLORS = Arrays.asList("#ff6961", "#aec6cf", "#77dd77", "#fcd670");

    // Shared variables
    private static Stage stage;
    private static GameState state;
    private static boolean isServer;
    private static LobbyScene lobbyScene;
    private static GameScene gameScene;
    private static Scene currentScene;
    private static Player currentPlayer;

    // Server variables
    private static HashSet<ObjectOutputStream> serverWriters = new HashSet<>(); // For sending messages to clients
    private static int nextPlayerId = 0; // Don't need to pass this to new server as no new players can join after the game has started

    // For client that becomes server
    private static int expectedClientCount;
    private static int clientCount;

    // Client variables
    private static ObjectOutputStream clientWriter; // For sending messages to the server

    // Getters
    public synchronized static GameState getState() {
        return state;
    }
    public synchronized static Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Shared methods
     */

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        state = new GameState(NUM_ROWS);

        MenuScene menuScene = new MenuScene();

        currentScene = Scene.LOGIN;
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
        currentScene = Scene.LOBBY;

        addHostAsPlayer();

        listenForClients(NUM_CLIENTS);
    }

    private static void listenForClients(int numClients) {
        Thread wait = new Thread(() -> {
            try {
                int count = 0;
                ServerSocket server = new ServerSocket(PORT);

                while (count < numClients) {
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
        currentScene = Scene.LOBBY;

        new ClientListener(true).start();
    }

    /**
     * Server methods
     */

    private static synchronized void addHostAsPlayer() {
        String name = "THE HOST";
        String ip = "Host's IP Address";
        int id = nextPlayerId;
        String color = COLORS.get(id);

        nextPlayerId++;

        Player player = new Player(id, name, ip, color);
        currentPlayer = player;
        state.addPlayer(player);

        lobbyScene.updateUI(Main.state);
        broadcastState();
    }

    private static synchronized void broadcastState() {
        UpdateStateMessage message = new UpdateStateMessage(state);
        broadcastMessage(message);
    }

    private static synchronized void broadcastMessage(Message message) {
        try {
            for (ObjectOutputStream writer: serverWriters) {
                writer.writeObject(message);
                writer.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void onStartClicked() {
        if (isServer) {
            gameScene = new GameScene(NUM_ROWS);
            currentScene = Scene.GAME;
            stage.setScene(gameScene);

            StartGameMessage startGameMessage = new StartGameMessage();
            broadcastMessage(startGameMessage);
        }
    }

    public static synchronized void onMessageReceivedFromClient(Message message, ObjectOutputStream writer) {
        switch (message.getType()) {
            case REQUEST_CONNECTION: {
                RequestConnectionMessage requestConnectionMessage = (RequestConnectionMessage) message;
                String name = requestConnectionMessage.getName();
                String ip = requestConnectionMessage.getIp();
                int id = nextPlayerId;

                String color = COLORS.get(id);

                Player player = new Player(id, name, ip, color);
                state.addPlayer(player);
                serverWriters.add(writer);

                try {
                    ConfirmConnectionMessage confirmConnectionMessage = new ConfirmConnectionMessage(player);
                    writer.writeObject(confirmConnectionMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                lobbyScene.updateUI(Main.state);
                broadcastState();
                nextPlayerId++;
                break;
            }
            case REQUEST_BOX_RESET: {
                // TODO: Might need to check if box is free
                RequestBoxResetMessage requestBoxResetMessage = (RequestBoxResetMessage) message;
                int requestBoxRow = requestBoxResetMessage.getRow();
                int requestBoxColumn = requestBoxResetMessage.getColumn();

                state.resetBox(requestBoxRow, requestBoxColumn);
                gameScene.updateUI(Main.state);
                broadcastState();

                break;
            }
            case REQUEST_BOX_FILLED: {
                RequestBoxFilledMessage requestBoxFilledMessage = (RequestBoxFilledMessage) message;
                int requestBoxRow = requestBoxFilledMessage.getRow();
                int requestBoxColumn = requestBoxFilledMessage.getColumn();
                int requestOwnerId = requestBoxFilledMessage.getOwnerId();
                String requestBoxColorString = requestBoxFilledMessage.getBoxColorString();

                Box box = Main.getState().getBox(requestBoxRow, requestBoxColumn);
                int boxOwnerId = box.getOwnerId();
                BoxStatus boxStatus = box.getStatus();

                if (boxStatus.equals(BoxStatus.FREE) || (boxStatus.equals(BoxStatus.RESERVED) && boxOwnerId == requestOwnerId)) {
                    state.fillBox(requestBoxRow, requestBoxColumn, requestOwnerId, requestBoxColorString);
                    gameScene.updateUI(Main.state);
                    broadcastState();
                }

                break;
            }
            case REQUEST_BOX_RESERVED: {
                RequestBoxReservedMessage requestBoxReservedMessage = (RequestBoxReservedMessage) message;
                int requestBoxRow = requestBoxReservedMessage.getRow();
                int requestBoxColumn = requestBoxReservedMessage.getColumn();
                int requestOwnerId = requestBoxReservedMessage.getOwnerId();
                String requestBoxColorString = requestBoxReservedMessage.getBoxColorString();

                Box box = Main.getState().getBox(requestBoxRow, requestBoxColumn);
                int boxOwnerId = box.getOwnerId();
                BoxStatus boxStatus = box.getStatus();

                if (boxStatus.equals(BoxStatus.FREE)) {
                    state.reserveBox(requestBoxRow, requestBoxColumn, requestOwnerId, requestBoxColorString);
                    gameScene.updateUI(Main.state);
                    broadcastState();
                }

                break;
            }
            case RECONNECT: {
                // TODO: handle a case when there's only one player remaining
                clientCount++;
                serverWriters.add(writer);

                broadcastState();

                if (clientCount == expectedClientCount) {
                    currentScene = Scene.GAME;

                    Platform.runLater(() -> {
                        stage.setScene(gameScene);
                    });

                    ResumeGameMessage resumeGameMessage = new ResumeGameMessage();
                    broadcastMessage(resumeGameMessage);
                }
            }
        }
    }


    /**
     * Client methods
     */

    public static synchronized void setClientWriter(ObjectOutputStream writer) {
        clientWriter = writer;
    }

    public static synchronized void onMessageReceivedFromServer(Message message) {
        switch (message.getType()) {
            case CONFIRM_CONNECTION: {
                ConfirmConnectionMessage confirmConnectionMessage = (ConfirmConnectionMessage) message;
                Player player = confirmConnectionMessage.getPlayer();
                currentPlayer = player;
                break;
            }
            case START_GAME: {
                gameScene = new GameScene(NUM_ROWS);
                currentScene = Scene.GAME;

                Platform.runLater(() -> {
                    stage.setScene(gameScene);
                });
                break;
            }
            case RESUME_GAME: {
                currentScene = Scene.GAME;
                gameScene.updateUI(Main.state);

                Platform.runLater(() -> {
                    stage.setScene(gameScene);
                });

                break;
            }
            case UPDATE_STATE: {
                UpdateStateMessage updateStateMessage = (UpdateStateMessage) message;
                Main.state = updateStateMessage.getState();

                if (currentScene.equals(Scene.LOBBY)) {
                    lobbyScene.updateUI(Main.state);
                } else if (currentScene.equals(Scene.GAME)) {
                    gameScene.updateUI(Main.state);
                }
                break;
            }
        }
    }

    // TODO: handle a case when a client disconnects

    public static void onServerDisconnected() {
        // Switch to Reconnecting scene
        ReconnectingScene reconnectingScene = new ReconnectingScene();
        currentScene = Scene.RECONNECTING;

        Platform.runLater(() -> {
            stage.setScene(reconnectingScene);
        });

        int previousHostId = state.getPlayers().get(0).getId();
        int currentPlayerId = currentPlayer.getId();

        // If the current player is the second in the player list, then become the server
        if (previousHostId + 1 == currentPlayerId) {
            state.getPlayers().remove(0); // Remove previous host from players
            isServer = true;
            expectedClientCount = state.getPlayers().size() - 1;
            clientCount = 0;
            listenForClients(expectedClientCount);
        } else {
            // Other clients wait for new server to setup
            new ClientListener(false).start();
        }
    }

    /**
     * Shared methods
     */

    // Gets called by a BoxView when the user cancels drawing the box
    public static synchronized void onBoxReset(int row, int column) {
        if (isServer) {
            state.resetBox(row, column);
            broadcastState();
        } else {
            try {
                RequestBoxResetMessage requestBoxResetMessage = new RequestBoxResetMessage(row, column);
                clientWriter.writeObject(requestBoxResetMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Gets called by a BoxView when the box has been drawn
    public static synchronized void onBoxReserved(int row, int column) {
        if (isServer) {
            state.reserveBox(row, column, currentPlayer.getId(), currentPlayer.getColorString());
            broadcastState();
        } else {
            try {
                RequestBoxReservedMessage requestBoxReservedMessage = new RequestBoxReservedMessage(row, column, currentPlayer.getId(), currentPlayer.getColorString());
                clientWriter.writeObject(requestBoxReservedMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Gets called by a BoxView when the box is filled
    public static synchronized void onBoxFilled(int row, int column) {
        if (isServer) {
            state.fillBox(row, column, currentPlayer.getId(), currentPlayer.getColorString());
            broadcastState();
        } else {
            try {
                RequestBoxFilledMessage requestBoxFilledMessage = new RequestBoxFilledMessage(row, column, currentPlayer.getId(), currentPlayer.getColorString());
                clientWriter.writeObject(requestBoxFilledMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

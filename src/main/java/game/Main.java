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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/* This is the entry point to the game */
public class Main extends Application {

    // Constants
    public static final int PORT = 8888;
    public static String MY_IP_ADDRESS = "";
    private static final List<String> COLORS = Arrays.asList("#ff6961", "#aec6cf", "#77dd77", "#fcd670");

    // Shared variables
    private static Stage stage;
    private static GameState state;
    public static boolean isServer;
    private static LobbyScene lobbyScene;
    private static GameScene gameScene;
    private static SceneType currentSceneType;
    private static Player currentPlayer;

    // Server variables
    private static HashSet<ObjectOutputStream> serverWriters = new HashSet<>(); // For sending messages to clients
    private static int nextPlayerId = 0; // Don't need to pass this to new server as no new players can join after the
                                         // game has started

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

        try {
            MY_IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        MenuScene menuScene = new MenuScene();

        currentSceneType = SceneType.LOGIN;
        stage.setScene(menuScene);
        stage.setTitle("Menu");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        if (!isServer) {
            ClientDisconnectMessage clientDisconnectMessage = new ClientDisconnectMessage(currentPlayer);
            clientWriter.writeObject(clientDisconnectMessage);
        }

        stage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // when user clicked server in the startup UI
    public static void onServerClicked(String name, int numPlayers, double minPercentage, double lineWidth,
            int numRows) {
        isServer = true;
        lobbyScene = new LobbyScene();

        // switch the UI to lobby scene
        stage.setScene(lobbyScene);
        stage.setTitle("Lobby");
        currentSceneType = SceneType.LOBBY;

        // create a brand new games state based on the server configuration
        state = new GameState(numRows, minPercentage, lineWidth);

        addHostAsPlayer(name);

        // starts multiple server threads to communicate with clients
        listenForClients(numPlayers - 1);
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

    // This method is invoked when user has clicked on "Client" button at startup UI
    public static void onClientClicked(String name, String serverIp) {
        isServer = false;

        // switch to lobby scene
        lobbyScene = new LobbyScene();
        stage.setScene(lobbyScene);
        stage.setTitle("Lobby");
        currentSceneType = SceneType.LOBBY;

        // start listening to the server
        new ClientListener(true, name, serverIp).start();
    }

    /**
     * Server methods
     */

    private static synchronized void addHostAsPlayer(String name) {
        String ip = Main.MY_IP_ADDRESS;
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
        for (ObjectOutputStream writer : serverWriters) {
            try {
                writer.writeObject(message);
                writer.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void onStartClicked() {
        if (isServer) {
            gameScene = new GameScene(state.getNumRows());
            currentSceneType = SceneType.GAME;
            stage.setScene(gameScene);
            stage.setTitle("Game");

            gameScene.updateUI(Main.state);

            StartGameMessage startGameMessage = new StartGameMessage();
            broadcastMessage(startGameMessage);
        }
    }

    // This method is invoked every time server recieves a new message from a client
    public static synchronized void onMessageReceivedFromClient(Message message, ObjectOutputStream writer) {
        switch (message.getType()) {
        case REQUEST_CONNECTION: {
            RequestConnectionMessage requestConnectionMessage = (RequestConnectionMessage) message;
            String name = requestConnectionMessage.getName();
            String ip = requestConnectionMessage.getIp();

            // picks a id for the client
            int id = nextPlayerId;

            // picks a color for the client
            String color = COLORS.get(id);
            Player player = new Player(id, name, ip, color);
            state.addPlayer(player);

            // adds writer wihch is an Output stream for the client. This allows host
            // to send message to the client in Main
            serverWriters.add(writer);

            try {
                // this sends the player object made for the client back to the client
                ConfirmConnectionMessage confirmConnectionMessage = new ConfirmConnectionMessage(player);
                writer.writeObject(confirmConnectionMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            lobbyScene.updateUI(Main.state);

            // broad cast its own game state to clients
            broadcastState();
            nextPlayerId++;
            break;
        }
        case REQUEST_BOX_RESET: {
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

            if (boxStatus.equals(BoxStatus.FREE)
                    || (boxStatus.equals(BoxStatus.RESERVED) && boxOwnerId == requestOwnerId)) {
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
        case RECONNECT: { // when a client wats to reconnect with a new host (after the old host has
                          // disconnected)
            clientCount++;
            serverWriters.add(writer);

            broadcastState();

            if (clientCount == expectedClientCount) {
                currentSceneType = SceneType.GAME;

                Platform.runLater(() -> {
                    stage.setScene(gameScene);
                    stage.setTitle("Game");
                });

                ResumeGameMessage resumeGameMessage = new ResumeGameMessage();
                broadcastMessage(resumeGameMessage);
            }
            break;
        }
        case CLIENT_DISCONNECT: {
            ClientDisconnectMessage clientDisconnectMessage = (ClientDisconnectMessage) message;
            Player disconnectedPlayer = clientDisconnectMessage.getDisconnectedPlayer();

            state.getPlayers().removeIf(player -> player.getId() == disconnectedPlayer.getId());
            serverWriters.remove(writer);

            gameScene.updateUI(Main.state);
            broadcastState();
            break;
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
        case CONFIRM_CONNECTION: { // when receiving the confirm message from server, this will happen after a
                                   // client send REQUEST_CONNECTION to server
            ConfirmConnectionMessage confirmConnectionMessage = (ConfirmConnectionMessage) message;
            Player player = confirmConnectionMessage.getPlayer();
            currentPlayer = player;
            break;
        }
        case START_GAME: { // when server clicks the start game button in lobby scene
            gameScene = new GameScene(state.getNumRows());
            gameScene.updateUI(Main.state);
            currentSceneType = SceneType.GAME;

            Platform.runLater(() -> {
                stage.setScene(gameScene);
                stage.setTitle("Game");
            });
            break;
        }
        case RESUME_GAME: { // when a client has become a server
            currentSceneType = SceneType.GAME;
            gameScene.updateUI(Main.state);

            Platform.runLater(() -> {
                stage.setScene(gameScene);
                stage.setTitle("Game");
            });

            break;
        }
        case UPDATE_STATE: { // when server broadcasts game state to all clients
            UpdateStateMessage updateStateMessage = (UpdateStateMessage) message;
            Main.state = updateStateMessage.getState();

            if (currentSceneType.equals(SceneType.LOBBY)) {
                lobbyScene.updateUI(Main.state);
            } else if (currentSceneType.equals(SceneType.GAME)) {
                gameScene.updateUI(Main.state);
            }
            break;
        }
        }
    }

    // This method will be invoked when the server has disconnected
    public static void onServerDisconnected() {
        // This means the current client is the only player left in the game
        if (state.getPlayers().size() == 2) {
            state.getPlayers().remove(0); // Remove server that has disconnected
            state.resetReservedBoxes();
            isServer = true;
            gameScene.updateUI(Main.state);

            // This prevents the client from switching to reconnecting scene unnecessarily
            return;
        }

        // Switch to Reconnecting scene
        ReconnectingScene reconnectingScene = new ReconnectingScene();
        currentSceneType = SceneType.RECONNECTING;

        Platform.runLater(() -> {
            stage.setScene(reconnectingScene);
            stage.setTitle("Reconnecting");
        });

        int previousHostId = state.getPlayers().get(0).getId();
        int currentPlayerId = currentPlayer.getId();

        // If the current player is the second in the player list, then become the
        // server
        if (previousHostId + 1 == currentPlayerId) {
            state.getPlayers().remove(0); // Remove previous host from players
            state.resetReservedBoxes();

            isServer = true;
            expectedClientCount = state.getPlayers().size() - 1;
            clientCount = 0;

            gameScene.updateUI(Main.state);
            listenForClients(expectedClientCount);
        } else {
            // Other clients wait for new server to setup
            new ClientListener(false, currentPlayer.getName(), state.getPlayers().get(1).getIp()).start();
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
                RequestBoxReservedMessage requestBoxReservedMessage = new RequestBoxReservedMessage(row, column,
                        currentPlayer.getId(), currentPlayer.getColorString());
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
            gameScene.updateUI(Main.state);
            broadcastState();
        } else {
            try {
                RequestBoxFilledMessage requestBoxFilledMessage = new RequestBoxFilledMessage(row, column,
                        currentPlayer.getId(), currentPlayer.getColorString());
                clientWriter.writeObject(requestBoxFilledMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

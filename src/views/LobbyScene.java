package views;

import controllers.GameController;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class LobbyScene extends Scene {

    GameController parent;

    public LobbyScene(GameController parent) {
        super(new VBox());

        this.parent = parent;
        setupUI();
    }

    public LobbyScene(GameController parent, String serverIp) {
        super(new VBox());

        this.parent = parent;
        setupUI();
    }

    void setupUI() {
        Label player1 = new Label("Player 1");
        Label player2 = new Label("Player 2");
        Button readyButton = new Button("Ready");

        VBox layout = new VBox(10, player1, player2, readyButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 10");

        this.setRoot(layout);
    }

//    private void startServer() {
//        Runnable server = () -> runServer();
//        Thread t = new Thread(server);
//        t.setDaemon(true);
//        t.start();
//    }
//
//    private void runServer() {
//        try {
//            ServerSocket ss = new ServerSocket(8888);
//            Socket s = ss.accept();
//
//            Platform.runLater(() -> statusLabel.setText("Connected"));
//
//            DataInputStream in = new DataInputStream(s.getInputStream());
//            String request = in.readUTF();
//            Platform.runLater(() -> System.out.println("Request: " + request));
//
//            String request2 = in.readUTF();
//            System.out.println("Request: " + request2);
////            Platform.runLater(() -> System.out.println("Request: " + request2));
//
//            System.out.println("Will this get called first?");
//
//
//            s.close();
//        } catch (Exception e) {
//            System.err.println("Unable to start the server");
//            e.printStackTrace();
//        }
//    }
}

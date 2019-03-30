package game.networking;

import game.Main;
import game.models.GameState;
import game.models.Player;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class ClientListener extends Thread  {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public void run() {
        try {
            socket = new Socket(Main.HOSTNAME, Main.PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            Main.onConnectedToServer(output);

            Player player = new Player();
            player.setIpAddress("Client's IP Address " + rand());
            player.setName("THE CLIENT " + rand());
            player.setId(0);

            output.writeObject(player);
            output.flush();

            while (socket.isConnected()) {
                GameState state = (GameState) input.readObject();
                Main.onGameStateUpdated(state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int rand() {
        Random random = new Random();
        return random.nextInt(100);
    }
}

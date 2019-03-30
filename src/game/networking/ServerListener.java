package game.networking;

import game.Main;
import game.models.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerListener extends Thread {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public ServerListener(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            Player player = (Player) input.readObject();
            Main.onPlayerConnected(player, output);

            while (socket.isConnected()) {
                String msg = (String) input.readObject();
                System.out.println("Message from client: " + msg);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package game.networking;

import game.Main;
import game.models.GameState;
import game.models.Message;
import game.models.Player;
import game.models.RequestConnectionMessage;

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

            Main.setClientWriter(output);

            String name = "THE CLIENT " + rand();
            String ip = "Client's IP Address " + rand();
            RequestConnectionMessage requestConnectionMessage = new RequestConnectionMessage(name, ip);

            output.writeObject(requestConnectionMessage);
            output.flush();

            while (socket.isConnected()) {
                Message message = (Message) input.readObject();
                Main.onMessageReceivedFromServer(message);
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

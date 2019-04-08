package game.networking;

import game.Main;
import game.models.messages.Message;
import game.models.messages.MessageType;

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

            while (socket.isConnected()) {
                Message message = (Message) input.readObject();

                Main.onMessageReceivedFromClient(message, output);

                // If client disconnects, close sockets and end this thread
                if (message.getType().equals(MessageType.CLIENT_DISCONNECT)) {
                    closeSockets();
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSockets() {
        try {
            output.close();
            input.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package game.networking;

import game.Main;
import game.models.messages.Message;
import game.models.messages.ReconnectMessage;
import game.models.messages.RequestConnectionMessage;
import game.scenes.ReconnectingScene;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class ClientListener extends Thread  {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean newConnection;
    private String serverIp;

    public ClientListener(boolean newConnection, String serverIp) {

        this.newConnection = newConnection;
        this.serverIp = serverIp;
    }

    public void run() {
        try {
            // If reconnecting, wait for some time for new server to set up
            if (!newConnection) {
                Thread.sleep(1000);
            }

            socket = new Socket(serverIp, Main.PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            Main.setClientWriter(output);

            if (newConnection) {
                String name = "THE CLIENT " + rand();
                String ip = InetAddress.getLocalHost().getHostAddress();
                RequestConnectionMessage requestConnectionMessage = new RequestConnectionMessage(name, ip);

                output.writeObject(requestConnectionMessage);
                output.flush();
            } else {
                ReconnectMessage reconnectMessage = new ReconnectMessage();
                output.writeObject(reconnectMessage);
                output.flush();
            }

            while (socket.isConnected()) {
                Message message = (Message) input.readObject();
                Main.onMessageReceivedFromServer(message);
            }
        } catch (EOFException e) {
            closeSockets();
            Main.onServerDisconnected();
        } catch (Exception e) {
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

    private int rand() {
        Random random = new Random();
        return random.nextInt(100);
    }
}

package communicationmod.networking;

import savestate.SaveState;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class AiClient {
    private static final String HOST_IP = "127.0.0.1";
    private static final int PORT = 5000;

    private final Socket socket;

    public AiClient() throws IOException {
        socket = new Socket(HOST_IP, PORT);
    }

    public void sendState() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(new SaveState().encode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

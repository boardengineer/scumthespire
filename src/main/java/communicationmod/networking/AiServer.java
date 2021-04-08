package communicationmod.networking;

import communicationmod.CommunicationMod;
import communicationmod.savestate.SaveState;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiServer {
    public static final int PORT_NUMBER = 5000;

    public AiServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);

                Socket socket = serverSocket.accept();

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                        .getInputStream()));

                while(true) {
                    if(CommunicationMod.battleAiController == null) {
                        CommunicationMod.saveState = new SaveState(in.readUTF());

                        CommunicationMod.shouldStartAiFromServer = true;
                        CommunicationMod.readyForUpdate = true;
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

package battleaimod.networking;

import battleaimod.battleai.CardCommand;
import battleaimod.battleai.Command;
import battleaimod.battleai.EndCommand;
import battleaimod.savestate.SaveState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiClient {
    private static final String HOST_IP = "127.0.0.1";
    private static final int PORT = 5000;

    private final Socket socket;

    public AiClient() throws IOException {
        socket = new Socket(HOST_IP, PORT);
    }

    public void sendState() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(new SaveState().encode());

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                        .getInputStream()));

                socket.setSoTimeout(5000);

                String readLine = "";

                while (!readLine.equals("DONE!!!")) {
                    try {
                        readLine = in.readUTF();
                    } catch (SocketTimeoutException e) {
                        System.err.println("Server failed to response after 5 seconds");
                    }

                    try {
                        JsonObject parsed = new JsonParser().parse(readLine).getAsJsonObject();
                        if (parsed.get("type").getAsString().equals("COMMAND_LIST")) {
                            JsonArray jsonCommands = parsed.get("commands").getAsJsonArray();
                            for (JsonElement jsonCommand : jsonCommands) {
                                String commandString = jsonCommand.getAsString();
                                Command toAdd = decodeCommandString(commandString);

                                if (toAdd != null) {
                                    System.err.println("The server wants to " + toAdd);
                                }
                            }

                        }
                        System.err.println("Server sent proper json message");

                    } catch (IllegalStateException e) {
                        // Not a json string
                    }
                }

                System.err.println("Received done");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static Command decodeCommandString(String commandString) {
        String type = new JsonParser().parse(commandString).getAsJsonObject()
                                      .get("type").getAsString();

        if (type.equals("CARD")) {
            return new CardCommand(commandString);
        } else if (type.equals("END")) {
            return new EndCommand();
        }

        return null;
    }
}

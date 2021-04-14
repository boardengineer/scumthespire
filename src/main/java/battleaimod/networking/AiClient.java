package battleaimod.networking;

import battleaimod.BattleAiMod;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.CardCommand;
import battleaimod.battleai.Command;
import battleaimod.battleai.EndCommand;
import battleaimod.savestate.SaveState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.exordium.Hexaghost;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiClient {
    private static final String HOST_IP = "127.0.0.1";
    private static final int PORT = 5000;

    private final Socket socket;

    public AiClient() throws IOException {
        socket = new Socket();
        socket.setSoTimeout(3000);

        try {
            socket.connect(new InetSocketAddress(HOST_IP, PORT));
        } catch (SocketTimeoutException e) {
            socket.close();
        }
    }

    public void sendState() {
        final SaveState state = new SaveState();
        state.loadState();
        AbstractDungeon.player.hand.refreshHandLayout();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(state.encode());

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                        .getInputStream()));

                socket.setSoTimeout(5000);

                String readLine = "";

                while (!readLine.equals("DONE!!!")) {
                    try {
                        readLine = in.readUTF();
                    } catch (SocketTimeoutException e) {
                        System.err.println("Server failed to response after 5 seconds");
                        continue;
                    }

                    try {
                        JsonObject parsed = new JsonParser().parse(readLine).getAsJsonObject();
                        if (parsed.get("type").getAsString().equals("COMMAND_LIST")) {
                            ArrayList<Command> commandsFromServer = new ArrayList<>();
                            JsonArray jsonCommands = parsed.get("commands").getAsJsonArray();
                            for (JsonElement jsonCommand : jsonCommands) {
                                Command toAdd = toCommand(jsonCommand);
                                commandsFromServer.add(toAdd);
                            }

                            System.err.println(commandsFromServer);
                            BattleAiMod.battleAiController = new BattleAiController(state, commandsFromServer);
                            BattleAiMod.readyForUpdate = true;
                            BattleAiMod.forceStep = true;
                        } else if (parsed.get("type").getAsString().equals("STATUS_UPDATE")) {
                            Hexaghost ghost;
                            System.err
                                    .println("Server says " + parsed.get("message").getAsString());
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

    private static Command toCommand(JsonElement jsonElement) {
        if (jsonElement.isJsonNull()) {
            return null;
        }
        String commandString = jsonElement.getAsString();
        String type = new JsonParser().parse(commandString).getAsJsonObject()
                                      .get("type").getAsString();

        if (type.equals("CARD")) {
            return new CardCommand(commandString);
        } else if (type.equals("END")) {
            return new EndCommand(commandString);
        }

        return null;
    }
}

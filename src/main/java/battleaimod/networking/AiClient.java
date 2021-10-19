package battleaimod.networking;

import battleaimod.BattleAiMod;
import battleaimod.battleai.CommandRunnerController;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.LudicrousSpeedMod;
import ludicrousspeed.simulator.commands.*;
import savestate.SaveState;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiClient {
    private static final String HOST_IP = "127.0.0.1";
    public static int fileIndex = 0;

    private final Socket socket;

    public AiClient() throws IOException {
        socket = new Socket();
        socket.setSoTimeout(3000);

        try {
            socket.connect(new InetSocketAddress(HOST_IP, AiServer.PORT_NUMBER));
        } catch (SocketTimeoutException e) {
            System.err.println("Failed on connect timeout");
            socket.close();
        }
    }

    public void sendState() {
        sendState(10_000);
    }

    public void sendState(int numTurns) {
        final SaveState state = new SaveState();

        AbstractDungeon.player.hand.refreshHandLayout();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                String encodedState = state.encode();

                try {
                    String fileName = String.format("startstates/%s.txt", fileIndex++);
                    FileWriter writer = new FileWriter(fileName);
                    writer.write(encodedState);
                    writer.close();

                    JsonObject runRequest = new JsonObject();
                    runRequest.addProperty("fileName", fileName);
                    runRequest.addProperty("num_turns", numTurns);

                    out.writeUTF(runRequest.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
                BattleAiMod.rerunController = null;

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                        .getInputStream()));

                socket.setSoTimeout(5000);

                String readLine = "";

                while (!readLine.equals(AiServer.doneString)) {

                    try {
                        readLine = in.readUTF();
                    } catch (SocketTimeoutException e) {
                        System.err.println("Server failed to respond after 5 seconds");
                        continue;
                    }

                    try {
                        JsonObject parsed = new JsonParser().parse(readLine).getAsJsonObject();
                        updateControllerForCommands(parsed);

                        if (parsed.has("message")) {
                            BattleAiMod.steveMessage = parsed.get("message").getAsString();
                        }

                    } catch (Exception e) {
                        // Not a json string
                    }
                }

                System.err.println("Received done");
            } catch (Exception e) {
                System.err.println("Server disconnected; clearing client for reset.");
                BattleAiMod.aiClient = null;
                BattleAiMod.rerunController = null;
            }
        });
    }

    private static Command toCommand(JsonElement jsonElement) {
        if (jsonElement.isJsonNull()) {
            return null;
        }

        JsonObject commandObject = new JsonParser()
                .parse(jsonElement.getAsJsonObject().get("command").getAsString())
                .getAsJsonObject();
        String type = commandObject.get("type").getAsString();
        String commandString = commandObject.toString();

        if (type.equals("CARD")) {
            if (jsonElement.getAsJsonObject().has("state")) {
                return new CardCommand(commandString, jsonElement.getAsJsonObject().get("state")
                                                                 .getAsString());
            }
            return new CardCommand(commandString);
        } else if (type.equals("POTION")) {
            if (jsonElement.getAsJsonObject().has("state")) {
                return new PotionCommand(commandString, jsonElement.getAsJsonObject().get("state")
                                                                   .getAsString());
            }
            return new PotionCommand(commandString);
        } else if (type.equals("END")) {
            if (jsonElement.getAsJsonObject().has("state")) {
                return new EndCommand(commandString, jsonElement.getAsJsonObject().get("state")
                                                                .getAsString());
            }
            return new EndCommand(commandString);
        } else if (type.equals("HAND_SELECT")) {
            return new HandSelectCommand(commandString);
        } else if (type.equals("HAND_SELECT_CONFIRM")) {
            return HandSelectConfirmCommand.INSTANCE;
        } else if (type.equals("GRID_SELECT")) {
            return new GridSelectCommand(commandString);
        }

        return null;
    }

    private static void updateControllerForCommands(JsonObject jsonMessage) {
        if (!jsonMessage.has("commands")) {
            return;
        }

        ArrayList<Command> commandsFromServer = new ArrayList<>();
        JsonArray jsonCommands = jsonMessage.get("commands").getAsJsonArray();
        for (JsonElement jsonCommand : jsonCommands) {
            Command toAdd = toCommand(jsonCommand);
            commandsFromServer.add(toAdd);
        }

        boolean complete = jsonMessage.get("type").getAsString()
                                      .equals(AiServer.commandListString);
        if (BattleAiMod.rerunController == null) {
            LudicrousSpeedMod.controller = BattleAiMod.rerunController = new CommandRunnerController(commandsFromServer, complete);
            BattleAiMod.forceStep = true;
        } else {
            BattleAiMod.rerunController
                    .updateBestPath(commandsFromServer, complete);
        }
    }
}

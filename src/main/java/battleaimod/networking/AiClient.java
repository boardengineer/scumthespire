package battleaimod.networking;

import battleaimod.BattleAiMod;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.commands.*;
import battleaimod.savestate.SaveState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

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
            System.err.println("Failed on connect timeout");
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
                BattleAiMod.battleAiController = null;

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                        .getInputStream()));

                socket.setSoTimeout(5000);

                String readLine = "";

                while (!readLine.equals("DONE!!!")) {
                    try {
                        readLine = in.readUTF();
                    } catch (SocketTimeoutException e) {
                        System.err.println("Server failed to respond after 5 seconds");
                        continue;
                    }

                    try {
                        JsonObject parsed = new JsonParser().parse(readLine).getAsJsonObject();

                        System.err.println("Server sent proper json message");

                        if (parsed.get("type").getAsString().equals("COMMAND_LIST")) {
                            System.err.println("Received final commands");

                            ArrayList<Command> commandsFromServer = new ArrayList<>();
                            JsonArray jsonCommands = parsed.get("commands").getAsJsonArray();
                            for (JsonElement jsonCommand : jsonCommands) {
                                System.err.println(jsonCommand);
                                Command toAdd = toCommand(jsonCommand);
                                System.err.println(toAdd);
                                commandsFromServer.add(toAdd);
                            }

                            System.err.println(commandsFromServer);

                            if (BattleAiMod.battleAiController == null) {
                                BattleAiMod.battleAiController = new BattleAiController(new SaveState(), commandsFromServer, true);
                                BattleAiMod.readyForUpdate = true;
                                BattleAiMod.forceStep = true;
                            } else {
                                BattleAiMod.battleAiController
                                        .updateBestPath(commandsFromServer, true);
                            }
                        } else if (parsed.get("type").getAsString().equals("STATUS_UPDATE")) {
                            String message = parsed.get("message").getAsString();
                            System.err.println(message);

                            if (parsed.has("commands")) {
                                ArrayList<Command> commandsFromServer = new ArrayList<>();
                                JsonArray jsonCommands = parsed.get("commands").getAsJsonArray();
                                for (JsonElement jsonCommand : jsonCommands) {
                                    Command toAdd = toCommand(jsonCommand);
                                    commandsFromServer.add(toAdd);
                                }

                                if (BattleAiMod.battleAiController == null) {
                                    BattleAiMod.battleAiController = new BattleAiController(new SaveState(), commandsFromServer, false);
                                    BattleAiMod.readyForUpdate = true;
                                    BattleAiMod.forceStep = true;
                                } else {
                                    BattleAiMod.battleAiController
                                            .updateBestPath(commandsFromServer, false);
                                }
                                System.err.println("current commands: " + commandsFromServer);
                            }

                            BattleAiMod.steveMessage = message;
//                            AbstractDungeon.effectList
//                                    .add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 2.0F, parsed.get("message").getAsString(), true));
                        }

                    } catch (IllegalStateException e) {
                        // Not a json string
                    }
                }

                System.err.println("Received done");
            } catch (IOException e) {
                System.err.println("Server disconnected; clearing client for reset.");
                BattleAiMod.aiClient = null;
                BattleAiMod.battleAiController = null;
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
        } else if (type.equals("POTION")) {
            return new PotionCommand(commandString);
        } else if (type.equals("END")) {
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
}

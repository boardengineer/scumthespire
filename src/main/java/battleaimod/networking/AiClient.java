package battleaimod.networking;

import battleaimod.BattleAiMod;
import battleaimod.battleai.CommandRunnerController;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import ludicrousspeed.LudicrousSpeedMod;
import ludicrousspeed.simulator.commands.*;
import savestate.SaveState;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AiClient {
    private static final String HOST_IP = "127.0.0.1";
    public static int fileIndex = 0;

    private final Socket socket;

    public AiClient() throws IOException {
        this(true);
    }

    public AiClient(boolean connect) throws IOException {
        socket = new Socket();
        socket.setSoTimeout(3000);

        if (connect) {
            try {
                socket.connect(new InetSocketAddress(HOST_IP, AiServer.PORT_NUMBER));
            } catch (SocketTimeoutException e) {
                System.err.println("Failed on connect timeout");
                socket.close();
            }
        }
    }

    public void sendState() {
        sendState(15_000);
    }

    public void sendState(String readFile) {
        try {
            JsonObject response = new JsonParser()
                    .parse(Files.lines(Paths.get(readFile)).collect(Collectors.joining()))
                    .getAsJsonObject();
            updateControllerForCommands(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendState(int numTurns) {
        final SaveState state = new SaveState();

        AbstractDungeon.player.hand.refreshHandLayout();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                String encodedState = state.jsonEncode().toString();

                try {

                    String directoryName = String
                            .format("C:/stuff/_ModTheSpire/startstates/%s/%02d", SeedHelper
                                    .getString(Settings.seed), AbstractDungeon.floorNum, fileIndex++);
                    File directory = new File(directoryName);
                    directory.mkdirs();

                    String fileName = directoryName + "/start.txt";

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
                e.printStackTrace();
                System.err.println("Server disconnected; clearing client for reset.");
                BattleAiMod.aiClient = null;
                BattleAiMod.rerunController = null;
            }
        });
    }

    public void runQueriedCommands(List<Command> commandsFromServer) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                BattleAiMod.rerunController = null;

                updateControllerForCommands(commandsFromServer);


                System.err.println("Received done");
            } catch (Exception e) {
                System.err.println("Server disconnected; clearing client for reset.");
                BattleAiMod.aiClient = null;
                BattleAiMod.rerunController = null;
            }
        });
    }

    public static Command toCommand(JsonElement jsonElement) {
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
        } else if (type.equals("GRID_SELECT_CONFIRM")) {
            return GridSelectConfrimCommand.INSTANCE;
        } else if (type.equals("CARD_REWARD_SELECT")) {
            return new CardRewardSelectCommand(commandString);
        }

        return null;
    }

    public static Command toCommand(String commandString) {
        JsonObject commandObject = new JsonParser()
                .parse(commandString)
                .getAsJsonObject();
        String type = commandObject.get("type").getAsString();

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

    private static void updateControllerForCommands(List<Command> commandsFromServer) {
        boolean complete = true;
        if (BattleAiMod.rerunController == null) {
            LudicrousSpeedMod.controller = BattleAiMod.rerunController = new CommandRunnerController(commandsFromServer, complete);
            BattleAiMod.forceStep = true;
        } else {
            BattleAiMod.rerunController
                    .updateBestPath(commandsFromServer, complete);
        }
    }
}

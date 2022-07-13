package battleaimod.networking;

import battleaimod.BattleAiMod;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.StateNode;
import battleaimod.battleai.TurnNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ludicrousspeed.LudicrousSpeedMod;
import savestate.SaveState;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AiServer {
    public static final int PORT_NUMBER = 5000;
    public static final String doneString = "DONE";

    public static final String statusUpdateString = "STATUS_UPDATE";
    public static final String commandListString = "COMMAND_LIST";

    public static int fileIndex = 0;

    public AiServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);

                Socket socket = serverSocket.accept();

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                        .getInputStream()));

                while (true) {
                    if (BattleAiMod.battleAiController == null) {
                        String requestFilePath = "";
                        String endSuffix = "\\end.txt";
                        try {
                            String runRequestString = in.readUTF();
                            JsonObject runRequest = new JsonParser().parse(runRequestString)
                                                                    .getAsJsonObject();

                            requestFilePath = runRequest.get("fileName").getAsString();
                            Path filePath = Paths.get(requestFilePath);

                            if (runRequest.has("end_suffix")) {
                                endSuffix = runRequest.get("end_suffix").getAsString();
                            }

                            String startState = Files.lines(filePath).collect(Collectors.joining());

                            BattleAiMod.requestedTurnNum = runRequest.get("num_turns").getAsInt();
                            BattleAiMod.saveState = new SaveState(startState);
                            BattleAiMod.saveState.initPlayerAndCardPool();

                            System.gc();

                            System.err.println("state parsed");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        BattleAiMod.shouldStartAiFromServer = true;
                        BattleAiMod.goFast = true;

                        // let the AI start before sending out requests
                        while (BattleAiMod.battleAiController == null) {
                            try {
                                System.err.println("waiting for controller to init...");
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        System.err.println("Controller Initiated");

                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                        // Still looking for route
                        while (BattleAiMod.battleAiController != null &&
                                !BattleAiMod.battleAiController.isDone()) {

                            // Send update
                            JsonObject jsonToSend = new JsonObject();
                            jsonToSend.addProperty("type", statusUpdateString);

                            TurnNode committedTurn = BattleAiMod.battleAiController.committedTurn();

                            if (committedTurn != null) {
                                JsonArray currentCommands = commandsForStateNode(committedTurn.startingState, false);
                                jsonToSend.add("commands", currentCommands);
                            }

                            jsonToSend.addProperty("message", String
                                    .format("%d / %d", BattleAiMod.battleAiController
                                            .turnsLoaded(), BattleAiMod.battleAiController
                                            .maxTurnLoads()));
                            out.writeUTF(jsonToSend.toString());

                            try {
                                Thread.sleep(BattleAiMod.MESSAGE_TIME_MILLIS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        System.err.println("BattleAI finished");

                        if (BattleAiMod.battleAiController != null && BattleAiMod.battleAiController
                                .isDone()) {
                            JsonObject jsonToSend = new JsonObject();
                            JsonArray commands = commandsForStateNode(BattleAiMod.battleAiController.bestEnd, true);

                            String endFileName = Paths.get(requestFilePath).getParent() + endSuffix;

                            FileWriter writer = new FileWriter(endFileName);
                            writer.write(BattleAiMod.battleAiController.bestEnd.saveState.encode());
                            writer.close();

                            // Send Command List
                            jsonToSend.addProperty("type", commandListString);
                            jsonToSend.add("commands", commands);

                            out.writeUTF(jsonToSend.toString());
                            LudicrousSpeedMod.controller = BattleAiMod.battleAiController = null;
                        } else {
                            System.err.println("This shouldn't have happened");
                        }

                        System.err.println("Sending done");
                        out.writeUTF(doneString);
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Client Disconnected, clearing server for reset");
                BattleAiMod.aiServer = null;
            }
        });
    }

    public static JsonArray commandsForStateNode(StateNode root, boolean shouldPrint) {
        JsonArray commands = new JsonArray();

        List<StateNode> stateNodes = BattleAiController.stateNodesToGetToNode(root);


        // Print the best path for debugging
        Iterator<StateNode> printIterator = stateNodes.iterator();

        while (printIterator.hasNext() && shouldPrint) {
            StateNode stateNode = printIterator.next();
            System.err.print(stateNode.lastCommand + " ");
        }

        Iterator<StateNode> bestPath = stateNodes.iterator();

        String stateDiffString = null;
        while (bestPath.hasNext()) {
            StateNode stateNode = bestPath.next();

            if (stateNode != null && stateNode.lastCommand != null) {
                JsonObject command = new JsonObject();

                command.addProperty("command", stateNode.lastCommand.encode());
                if (stateDiffString != null) {

                    try {
                        String fileName = String.format("savestates/%s.txt", fileIndex++);
                        FileWriter writer = new FileWriter(fileName);
                        writer.write(stateDiffString);
                        writer.close();
                        command.addProperty("state", fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                stateDiffString = stateNode.saveState.diffEncode();
                commands.add(command);
            } else {
                commands.add(JsonNull.INSTANCE);
            }
        }

        return commands;
    }
}

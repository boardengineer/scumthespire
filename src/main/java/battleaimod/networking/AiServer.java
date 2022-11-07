package battleaimod.networking;

import battleaimod.BattleAiMod;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.StateNode;
import battleaimod.battleai.TurnNode;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ludicrousspeed.LudicrousSpeedMod;
import savestate.SaveState;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AiServer {
    public static final int PORT_NUMBER = 5125;
    public static final String doneString = "DONE";

    public static final String statusUpdateString = "STATUS_UPDATE";
    public static final String commandListString = "COMMAND_LIST";

    public static int fileIndex = 0;

    public AiServer() {
        ThreadFactory namedThreadFactory =
                new ThreadFactoryBuilder().setNameFormat("server-networking-thread-%d").build();
        ExecutorService executor = Executors.newSingleThreadExecutor(namedThreadFactory);
        executor.submit(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);

                Socket socket = serverSocket.accept();

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                        .getInputStream()));

                while (true) {
                    fileIndex = 0;
                    if (BattleAiMod.battleAiController == null) {
                        String requestFilePath = "";
                        String endSuffix = "/end.txt";
                        String commandFileName = null;
                        boolean statesMatch = true;
                        boolean shouldWrite = false;
                        String newContents = "";
                        try {
                            String runRequestString = in.readUTF();
                            JsonObject runRequest = new JsonParser().parse(runRequestString)
                                                                    .getAsJsonObject();

                            System.err.println("runRequest is " + runRequest);

                            requestFilePath = runRequest.get("fileName").getAsString();
                            Path filePath = Paths.get(requestFilePath);

                            System.err.println("reading from " + requestFilePath);
                            System.err.println("filePath is " + filePath);

                            if (runRequest.has("end_suffix")) {
                                endSuffix = runRequest.get("end_suffix").getAsString();
                            }

                            if (runRequest.has("command_file")) {
                                commandFileName = runRequest.get("command_file").getAsString();
                            } else {
                                System.err.println("no command file path");
                            }

                            SaveState originalState = SaveState.forFile(filePath.toString());

                            BattleAiMod.requestedTurnNum = runRequest.get("num_turns")
                                                                     .getAsInt();
                            BattleAiMod.saveState = originalState;
                            BattleAiMod.saveState.initPlayerAndCardPool();

//                                System.err.println("start state equals: " + shouldWrite);
                            System.gc();

                            System.err.println("state parsed " + commandFileName);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }


                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

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

                        System.err.println("Battle Controller Started " + commandFileName);

                        int latestWrittenTurn = 0;

                        // Still looking for route
                        while (BattleAiMod.battleAiController != null &&
                                !BattleAiMod.battleAiController.isDone()) {
                            // Send update
                            JsonObject jsonToSend = new JsonObject();
                            jsonToSend.addProperty("type", statusUpdateString);

                            TurnNode committedTurn = BattleAiMod.battleAiController.committedTurn();

                            if (committedTurn != null) {
                                int committedTurnNumber = committedTurn.startingState.saveState.turn;

                                if (latestWrittenTurn < committedTurnNumber) {
                                    latestWrittenTurn = committedTurnNumber;
                                    JsonArray currentCommands = commandsForStateNode(committedTurn.startingState, false);
                                    jsonToSend.add("commands", currentCommands);
                                }
                            }

                            jsonToSend.addProperty("message", String
                                    .format("%d / %d", BattleAiMod.battleAiController
                                            .turnsLoaded(), BattleAiMod.battleAiController
                                            .maxTurnLoads()));

                            try {
                                out.writeUTF(jsonToSend.toString());
                            } catch (UTFDataFormatException e) {
                                // If the messages get too long just don't send.
                            }

                            try {
                                Thread.sleep(BattleAiMod.MESSAGE_TIME_MILLIS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }


                        System.err.println("BattleAI finished " + commandFileName);

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
                            jsonToSend
                                    .addProperty("predictor_damage", BattleAiMod.battleAiController.expectedDamage);

                            if (commandFileName != null) {
                                try {
                                    System.err
                                            .println("should be writing file to " + commandFileName);
                                    Path parent = Paths.get(commandFileName).getParent();
                                    new File(parent.toString()).mkdirs();
                                    FileWriter commandWriter = new FileWriter(commandFileName);
                                    commandWriter.write(jsonToSend.toString());
                                    commandWriter.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            try {
                                out.writeUTF(jsonToSend.toString());
                            } catch (UTFDataFormatException e) {
                                System.err
                                        .println("Result too big, removing commands and writing to file instead");
                                jsonToSend.remove("commands");

                                if (commandFileName != null) {
                                    System.err.println("returning with command path");
                                    jsonToSend.addProperty("command_path", commandFileName);
                                    out.writeUTF(jsonToSend.toString());
                                } else {
                                    System.err.println("commandFileName is null");
                                }
                            }
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
                        File toWrite = new File(fileName);
                        new File(toWrite.getParent()).mkdirs();

                        try (OutputStreamWriter writer =
                                     new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)) {
                            writer.write(stateDiffString);
                        }

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

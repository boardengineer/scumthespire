package battleaimod.networking;

import battleaimod.BattleAiMod;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.StateNode;
import battleaimod.battleai.TurnNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import ludicrousspeed.LudicrousSpeedMod;
import ludicrousspeed.simulator.commands.Command;
import savestate.SaveState;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AiServer {
    public static final int PORT_NUMBER = 5000;
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
                        try {
                            String fileName = in.readUTF();
                            String startState = Files.lines(Paths.get(fileName)).collect(Collectors
                                    .joining());

                            BattleAiMod.saveState = new SaveState(startState);

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
                        while (BattleAiMod.battleAiController != null && !BattleAiMod.battleAiController
                                .isDone()) {
                            // Send update

                            JsonObject jsonToSend = new JsonObject();

                            TurnNode committedTurn = BattleAiMod.battleAiController.committedTurn();
                            if (committedTurn != null) {
                                JsonArray currentCommands = commandsForStateNodeExperimental(committedTurn.startingState);
                                jsonToSend.add("commands", currentCommands);
                            }

                            jsonToSend.addProperty("type", "STATUS_UPDATE");
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
                            JsonArray commands = null;
                            try {
                                commands = commandsForStateNodeExperimental(BattleAiMod.battleAiController.bestEnd);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Send Command List
                            jsonToSend.addProperty("type", "COMMAND_LIST");
                            jsonToSend.add("commands", commands);

                            out.writeUTF(jsonToSend.toString());
                            LudicrousSpeedMod.controller = BattleAiMod.battleAiController = null;
                        } else {
                            System.err.println("This shouldn't have happened");
                        }

                        System.err.println("Sending done");
                        out.writeUTF("DONE!!!");
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Client Disconnected, clearing server for reset");
                BattleAiMod.aiServer = null;
            }
        });
    }

    public static JsonArray commandsForStateNode(StateNode stateNode) {
        JsonArray commands = new JsonArray();

        Iterator<Command> bestPath = BattleAiController.commandsToGetToNode(stateNode).iterator();
        while (bestPath.hasNext()) {
            Command nextCommand = bestPath.next();
            if (nextCommand != null) {
                commands.add(nextCommand.encode());
            } else {
                commands.add(JsonNull.INSTANCE);
            }
        }

        return commands;
    }

    public static JsonArray commandsForStateNodeExperimental(StateNode root) {
        JsonArray commands = new JsonArray();

        Iterator<StateNode> bestPath = BattleAiController.stateNodesToGetToNode(root).iterator();

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

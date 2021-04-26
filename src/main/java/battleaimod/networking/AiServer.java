package battleaimod.networking;

import battleaimod.BattleAiMod;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.commands.Command;
import battleaimod.battleai.StateNode;
import battleaimod.battleai.TurnNode;
import battleaimod.savestate.SaveState;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
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

                while (true) {
                    if (BattleAiMod.battleAiController == null) {
                        BattleAiMod.saveState = new SaveState(in.readUTF());

                        BattleAiMod.shouldStartAiFromServer = true;
                        BattleAiMod.readyForUpdate = true;
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
                        while (BattleAiMod.battleAiController != null && !BattleAiMod.battleAiController.runCommandMode) {
                            // Send update
                            JsonObject jsonToSend = new JsonObject();

                            TurnNode committedTurn = BattleAiMod.battleAiController.committedTurn;
                            if (committedTurn != null) {
                                JsonArray currentCommands = commandsForStateNode(committedTurn.startingState);
                                jsonToSend.add("commands", currentCommands);
                            }

                            jsonToSend.addProperty("type", "STATUS_UPDATE");
                            jsonToSend.addProperty("message", String
                                    .format("%d / %d", BattleAiMod.battleAiController.turnsLoaded, BattleAiMod.battleAiController.maxTurnLoads));
                            out.writeUTF(jsonToSend.toString());

                            try {
                                Thread.sleep(BattleAiMod.MESSAGE_TIME_MILLIS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        System.err.println("BattleAI finished");

                        if (BattleAiMod.battleAiController != null && BattleAiMod.battleAiController.runCommandMode) {
                            JsonObject jsonToSend = new JsonObject();
                            JsonArray commands = new JsonArray();


                            Iterator<Command> bestPath = BattleAiMod.battleAiController.bestPathRunner;
                            while (bestPath.hasNext()) {
                                Command nextCommand = bestPath.next();
                                if (nextCommand != null) {
                                    commands.add(nextCommand.encode());
                                } else {
                                    commands.add(JsonNull.INSTANCE);
                                }
                            }


                            // Send Command List
                            jsonToSend.addProperty("type", "COMMAND_LIST");
                            jsonToSend.add("commands", commands);

                            try {
                                System.err.println(BattleAiMod.battleAiController.bestPath);
                            } catch (NullPointerException e) {
                                System.err
                                        .println("Can't print best path, it was already cleared " + commands);
                            }

                            out.writeUTF(jsonToSend.toString());
                            BattleAiMod.battleAiController = null;
                        } else {
                            System.err.println("This shouldn't have happened");
                        }

                        System.err.println("Sending done");
                        out.writeUTF("DONE!!!");
                    }
                }


            } catch (IOException e) {
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
}

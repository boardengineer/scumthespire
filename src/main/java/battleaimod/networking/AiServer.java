package battleaimod.networking;

import battleaimod.BattleAiMod;
import battleaimod.battleai.Command;
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

                            jsonToSend.addProperty("type", "STATUS_UPDATE");
                            jsonToSend.addProperty("message", String
                                    .format("%d", BattleAiMod.battleAiController.turnsLoaded));
                            out.writeUTF(jsonToSend.toString());

                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

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

                            System.err.println("Sending commands " + commands);

                            out.writeUTF(jsonToSend.toString());
                            BattleAiMod.battleAiController = null;
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
}

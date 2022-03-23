package autoplay.battleaimod.server.networking;

import autoplay.battleaimod.core.networking.Constants;
import autoplay.battleaimod.core.networking.MessageType;
import autoplay.battleaimod.server.BattleAiServerMod;
import autoplay.battleaimod.server.battleai.BattleAiController;
import autoplay.battleaimod.server.battleai.StateNode;
import autoplay.battleaimod.server.battleai.TurnNode;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
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
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AiServer {

    private static int fileIndex = 0;

    public AiServer() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(() -> {
            try (ServerSocket serverSocket = new ServerSocket(Constants.PORT_NUMBER)) {

                Socket socket = serverSocket.accept();

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                        .getInputStream()));

                while (true) {
                    if (BattleAiServerMod.battleAiController == null) {
                        try {
                            String runRequestString = in.readUTF();
                            JsonObject runRequest = new JsonParser().parse(runRequestString)
                                                                    .getAsJsonObject();

                            String startState = Files
                                    .lines(Paths.get(runRequest.get("fileName").getAsString()))
                                    .collect(Collectors
                                            .joining());

                            BattleAiServerMod.requestedTurnNum = runRequest.get("num_turns").getAsInt();
                            BattleAiServerMod.saveState = new SaveState(startState);

                            System.err.println("state parsed");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        BattleAiServerMod.shouldStartAiFromServer = true;
                        BattleAiServerMod.goFast = true;

                        // let the AI start before sending out requests
                        while (BattleAiServerMod.battleAiController == null) {
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
                        while (BattleAiServerMod.battleAiController != null && !BattleAiServerMod.battleAiController
                                .isDone()) {

                            // Send update
                            JsonObject jsonToSend = new JsonObject();
                            jsonToSend.addProperty("type", MessageType.STATUS_UPDATE.name());

                            TurnNode committedTurn = BattleAiServerMod.battleAiController.committedTurn();
                            if (committedTurn != null) {
                                JsonArray currentCommands = commandsForStateNode(committedTurn.startingState, false);
                                jsonToSend.add("commands", currentCommands);
                            }

                            jsonToSend.addProperty("message", String
                                    .format("%d / %d", BattleAiServerMod.battleAiController
                                            .turnsLoaded(), BattleAiServerMod.battleAiController
                                            .maxTurnLoads()));
                            out.writeUTF(jsonToSend.toString());

                            try {
                                Thread.sleep(BattleAiServerMod.MESSAGE_TIME_MILLIS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        System.err.println("BattleAI finished");

                        if (BattleAiServerMod.battleAiController != null && BattleAiServerMod.battleAiController
                                .isDone()) {
                            JsonObject jsonToSend = new JsonObject();
                            JsonArray commands = commandsForStateNode(BattleAiServerMod.battleAiController.bestEnd, true);

                            // Send Command List
                            jsonToSend.addProperty("type", MessageType.COMMAND_LIST.name());
                            jsonToSend.add("commands", commands);

                            out.writeUTF(jsonToSend.toString());
                            LudicrousSpeedMod.controller = BattleAiServerMod.battleAiController = null;
                        } else {
                            System.err.println("This shouldn't have happened");
                        }

                        System.err.println("Sending done");
                        out.writeUTF(MessageType.DONE.name());
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Client Disconnected, clearing server for reset");
                BattleAiServerMod.aiServer = null;
                BattleAiServerMod.battleAiController = null;
            }
        }, 0, 1, TimeUnit.SECONDS);
        Gdx.app.addLifecycleListener(new ShutdownListener(executor));
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
                        try (FileWriter writer = new FileWriter(fileName)) {
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

    private static class ShutdownListener implements LifecycleListener {
        private final ScheduledExecutorService executor;

        public ShutdownListener(ScheduledExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public void dispose() {
               executor.shutdown();
        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }
    }
}

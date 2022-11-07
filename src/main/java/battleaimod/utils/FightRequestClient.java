package battleaimod.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FightRequestClient {
    private static final String HOST_IP = "127.0.0.1";
    static Socket socket = new Socket();
    public static final int PORT_NUMBER = 5125;
    public static final String DONE_STRING = "DONE";

    public static void main(String[] args) throws IOException {
        //String path = "C:\\stuff\\rundata\\runs\\T9R4D9U3ZY43\\27\\THE_SILENT.autosave";
        //fightSaveFile(path, "THE_SILENT");

        socket.setSoTimeout(3000);
        boolean connect = true;

        if (connect) {
            try {
                socket.connect(new InetSocketAddress(HOST_IP, PORT_NUMBER));
            } catch (SocketTimeoutException e) {
                System.err.println("Failed on connect timeout");
                socket.close();
            }
        }

        sendBackendFightRequest("C:\\stuff\\rundata\\runs\\T9R4D9U3ZY43\\27\\start.txt", "15000", null);
    }

    static void fightSaveFile(String filePath, String chararacter) {
        try {
            sendLoadRequest(filePath, chararacter);

            while (!sendStatusRequest().equals("READY")) {
                Thread.sleep(250);
            }
            Thread.sleep(2_000);

            sendStartRequest();

            long startWaitTime = System.currentTimeMillis();
            long startTimeout = startWaitTime + 20_000;

            while (!sendStatusRequest().equals("PROCESSING") && System
                    .currentTimeMillis() < startTimeout) {
                Thread.sleep(250);
            }
            // Escape timer is 3 seconds, wait 5 to make sure
            Thread.sleep(5_000);

            while (sendStatusRequest().equals("PROCESSING")) {
                Thread.sleep(250);
            }

            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void sendLoadRequest(String path, String playerClass) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(HOST_IP, 5200));

            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                JsonObject requestJson = new JsonObject();

                requestJson.addProperty("command", "load");
                requestJson.addProperty("path", path);
                requestJson.addProperty("playerClass", playerClass);

                out.writeUTF(requestJson.toString());
            } catch (SocketTimeoutException e) {
                System.err.println("Failed on connect timeout");
                socket.close();
            }

            DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                    .getInputStream()));

            socket.setSoTimeout(5000);

            String readLine = in.readUTF();

            System.err.println(readLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendStartRequest() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(HOST_IP, 5200));

            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                JsonObject requestJson = new JsonObject();

                requestJson.addProperty("command", "startAi");

                out.writeUTF(requestJson.toString());
            } catch (SocketTimeoutException e) {
                System.err.println("Failed on connect timeout");
                socket.close();
            }

            DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                    .getInputStream()));

            socket.setSoTimeout(5000);

            String readLine = in.readUTF();
            System.err.println(readLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String sendStatusRequest() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(HOST_IP, 5200));

            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                JsonObject requestJson = new JsonObject();

                requestJson.addProperty("command", "status");

                out.writeUTF(requestJson.toString());
            } catch (SocketTimeoutException e) {
                System.err.println("Failed on connect timeout");
                socket.close();
            }

            DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                    .getInputStream()));

            socket.setSoTimeout(5000);

            String readLine = in.readUTF();

            return readLine;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    static void sendBackendFightRequest(String filename, String numTurns, String commandFileName) throws IOException {
        JsonObject runRequest = new JsonObject();

        runRequest.addProperty("fileName", filename);
        runRequest.addProperty("num_turns", numTurns);

        Path filePath = Paths.get(filename);

        String startFileName = filePath.getParent() + "\\start.txt";
        String endFileName = filePath.getParent() + "\\end.txt";

        runRequest.addProperty("end_suffix", "\\end.txt");

        if (commandFileName != null) {
            runRequest.addProperty("command_file", commandFileName);
        }

        ParsedSaveState saveStateBefore = new ParsedSaveState(stateFromFile(startFileName));
        String className = saveStateBefore.getClassName();


        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(runRequest.toString());
        } catch (SocketTimeoutException e) {
            System.err.println("Failed on connect timeout");
            socket.close();
        }

        DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                .getInputStream()));

        socket.setSoTimeout(5000);

        String readLine = "";

        while (!readLine.equals(DONE_STRING)) {
            try {
                readLine = in.readUTF();
            } catch (SocketTimeoutException e) {
                System.err.println("Server failed to respond after 5 seconds");
                continue;
            }
        }
    }

    // Save State object used to parse state files without the need for loading the game or mods.
    private static class ParsedSaveState {
        final JsonObject saveStateJson;
        final JsonObject playerJson;
        final JsonObject playerCreatureJson;
        final JsonObject rngStateJson;

        ParsedSaveState(String jsonString) {
            this.saveStateJson = new JsonParser().parse(jsonString).getAsJsonObject();
            this.playerJson = saveStateJson.get("player_state").getAsJsonObject();
            this.playerCreatureJson = playerJson.get("creature").getAsJsonObject();
            this.rngStateJson = saveStateJson.get("rng_state").getAsJsonObject();
        }

        int getCurrentHp() {
            return playerCreatureJson.get("current_health").getAsInt();
        }

        int getFloorNum() {
            return saveStateJson.get("floor_num").getAsInt();
        }

        String getClassName() {
            return playerJson.get("chosen_class_name").getAsString();
        }

        long getSeed() {
            return rngStateJson.get("seed").getAsLong();
        }

        int getMaxHp() {
            return playerCreatureJson.get("max_health").getAsInt();
        }

        String getDeckString() {
            String cards = Stream.of(playerJson.get("master_deck").getAsString().split(";;;"))
                                 .map(s -> new JsonParser().parse(s).getAsJsonObject()
                                                           .get("card_id").getAsString())
                                 .filter(s -> !s.isEmpty()).collect(Collectors.joining(","));

            return String.format("\"%s\"", cards);
        }
    }

    private static String stateFromFile(String fileName) throws IOException {
        return Files.lines(Paths.get(fileName))
                    .collect(Collectors.joining());
    }
}

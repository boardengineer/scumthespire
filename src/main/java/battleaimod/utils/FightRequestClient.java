package battleaimod.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class FightRequestClient {
    public static HashSet<String> FORBIDDEN_RELIC_IDS = new HashSet<String>() {{
//        add("Achilles Heel Guard");
//        add("Blighted Snail");
//        add("Business Contract");
//        add("Clerics Golden Helm");
//        add("Comic Book");
//        add("Dice Of Fate");
//        add("Festivus Pole");
//        add("Filiform Needle");
//        add("Fresh Water");
//        add("Happy Flower Bed");
//        add("Horn Of Plenty");
//        add("Hotwired Cables");
//        add("Jade Mystic Knot");
//        add("Paper Bomb");
//        add("Preserved Amber");
//        add("PrismaticBranch");
//        add("Relic Compass");
//        add("Runic Replicator");
//        add("Sail");
//        add("Scales of Justice");
//        add("Snecko Charm");
//        add("Snecko Skin Boots");
//        add("Solid State Drive");
//        add("Tactical Harness");
//        add("Vampire Fang");
//        add("Venomous Scales");
//        add("Yin");
//        add("Toolbox");
//        add("hermit:BloodyTooth");
//        add("hermit:BrassTacks");
    }};

    private static final String HOST_IP = "127.0.0.1";
    static Socket socket = new Socket();
    public static final int PORT_NUMBER = 5125;
    public static final String DONE_STRING = "DONE";

    public static ArrayList<RunResult> runResults = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        /*
        String allSeedsDir = "C:/stuff/rundata/runs";

        int numSeeds = new File(allSeedsDir).list().length;

        System.err.println("num seeds : " + numSeeds);
        final int[] seedNum = {0, 0};

        Arrays.stream(new File(allSeedsDir).list()).forEach(dirStr -> {
            seedNum[1]++;
            if (seedNum[1] > 600 || seedNum[1] < 490) {
                return;
            }
            try {
                System.err.println("Seed Number: " + seedNum[0]++);
                runAllBattlesForSeed(dirStr);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("out.csv"), StandardCharsets.UTF_8))) {
            runResults.forEach(result -> {
                try {
                    writer.write(result.getCsvString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }*/

//        runAllBattlesForSeed("2C1SBJ6UA8PZJ"); // Gamble for days (recorded and uploaded)
//        runAllBattlesForSeed("4Y2MSAT52IMU6"); // Sample 1 (recorded)
//        runAllBattlesForSeed("12MWK6QZQ6M3I"); // Sample 2 (recorded)
//        runAllBattlesForSeed("3L9IBWMH6JM6X");

//        runAllBattlesForSeed("EL7EAMUU8HH4"); // Reprogram (recorded)
//        runAllBattlesForSeed("1AJNE74BS4XSG"); // broken
//        runAllBattlesForSeed("MAQ9UZBI0W40"); // F16 (too short)
//        runAllBattlesForSeed("4E3BYQ4WHD3L6"); // Sig move
//        runAllBattlesForSeed("4GHCWVYLX0RJF"); // Winstone (recorded)

        // macro samples
//        runAllBattlesForSeed("NCH68SATLPZT");
//        runAllBattlesForSeed("2S7AWMPY1DI3R");
//        runAllBattlesForSeed("373WGHXRLEA01");
//        runAllBattlesForSeed("3IFYTCZFXUI1P");
//        runAllBattlesForSeed("503D7D0NZ9XQR");
//        runAllBattlesForSeed("3IFECG0HVK7S4");
//        runAllBattlesForSeed("2GW2F5FCKL64C");

        //runAllBattlesForSeed("119HMD6Y7VTKV");

//        runAllBattlesForSeed("JPENLVL522MC");
//        runAllBattlesForSeed("4ETS18L4EL4US");
//        runAllBattlesForSeed("2V921K2QEUB29");
//        runAllBattlesForSeed("3N9IKUSYU0144");


//        runAllBattlesForSeed("2TG6S1NB8YWMH");
//        runAllBattlesForSeed("3M8NNS7X5BNEW");
//        runAllBattlesForSeed("1YVVM6LZK1X5A");
//        runAllBattlesForSeed("5CYYYW3E8SDCX");


//        runAllBattlesForSeed("9042FKURJ7E7");
//        runAllBattlesForSeed("4ND6UL5GT6HTB");
//        runAllBattlesForSeed("VTHA6KRZVD78");

//        runAllBattlesForSeed("1U1C3HUD51N81");
//        runAllBattlesForSeed("4BQMJLXHX443K");
//        runAllBattlesForSeed("2V1HUUG7CXQ5P");

        // Watcher against champ Gets stuck in wrath and gets executed.  Looks really good,
        // Expected 0 Damage.
//        String charClass = "WATCHER";
//        String seed = "1BV6WJIEMU1QH";
//        int floor = 33;


        // Failed ghost chad against champ.
//        String charClass = "RobotSpaceExplorer";
//        String charClass = "THE_SILENT";
//        String charClass = "DEFECT";
//        String charClass = "IRONCLAD";
//        String charClass = "WATCHER";
//        String charClass = "THE_CURSED";
        String charClass = "MARISA";
        String seed = "3NEWUYBBVJQ0M";
        int floor = 46;
//
        String path = String
                .format("C:\\stuff\\rundata\\runs\\%s\\%02d\\%s.autosave", seed, floor, charClass);
        fightSaveFile(path, charClass);
//        sendLoadRequest(path, charClass);

//        runAllBattlesForSeed("4Y2MSAT52IMU6" );

//        socket.setSoTimeout(3000);
//        boolean connect = true;
//
//        if (connect) {
//            try {
//                socket.connect(new InetSocketAddress(HOST_IP, PORT_NUMBER));
//            } catch (SocketTimeoutException e) {
//                System.err.println("Failed on connect timeout");
//                socket.close();
//            }
//        }
//
//        sendBackendFightRequest("C:\\stuff\\rundata\\runs\\T9R4D9U3ZY43\\27\\start.txt", "15000", null);
    }

    static void fightSaveFile(String filePath, String chararacter) {
        fightSaveFile(filePath, chararacter, 1, 0);
    }

    static void fightSaveFile(String filePath, String chararacter, int start, int end) {
        try {
            sendLoadRequest(filePath, chararacter, start, end);

            while (!sendStatusRequest().equals("READY")) {
                Thread.sleep(250);
            }
            Thread.sleep(1_000);

            Path path = Paths.get(filePath);

            String startFileName = path.getParent() + "\\start.txt";
            String commandFileName = path.getParent() + "\\commands.txt";

            sendStartRequest(commandFileName, startFileName);

            long startWaitTime = System.currentTimeMillis();
            long startTimeout = startWaitTime + 20_000;

            while (!sendStatusRequest().equals("PROCESSING") && System
                    .currentTimeMillis() < startTimeout) {
                Thread.sleep(250);
            }
            // Escape timer is 3 seconds, wait 5 to make sure
            Thread.sleep(5_000);

            while (sendStatusRequest().equals("PROCESSING")) {
                Thread.sleep(2500);
            }

            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class RunResult {
        String characterClass;
        String seed;
        int floorNum;

        int startingHp;
        int actualDamage;
        int predicatedDamage;

        String deckString;
        String potionString;
        String relicString;

        String enemiesString;
        int ascensionLevel;
        String fightName;

        public String getCsvString() {
            return String
                    .format("%s,%s,%s,%d,%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,%s", characterClass, seed, floorNum, startingHp, predicatedDamage, actualDamage, deckString, relicString, potionString, enemiesString, ascensionLevel, fightName);
        }
    }

    static void sendLoadRequest(String path, String playerClass) {
        sendLoadRequest(path, playerClass, 1, 0);
    }

    static void sendLoadRequest(String path, String playerClass, int start, int end) {
        try {
            System.err.println("should request loading " + start + " to " + end);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(HOST_IP, 5200));

            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                JsonObject requestJson = new JsonObject();

                requestJson.addProperty("command", "load");

                requestJson.addProperty("replay_floor_start", start);
                requestJson.addProperty("replay_floor_end", end);

                requestJson.addProperty("path", path);
                requestJson.addProperty("playerClass", playerClass);

                requestJson.addProperty("recall_mode", true);

                out.writeUTF(requestJson.toString());
            } catch (SocketTimeoutException e) {
                System.err.println("Failed on connect timeout");
                socket.close();
            }

            DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                    .getInputStream()));

            socket.setSoTimeout(5000);

            String readLine = in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendStartRequest(String preferredComandFilename, String preferredStartFilename) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(HOST_IP, 5200));

            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                JsonObject requestJson = new JsonObject();

                requestJson.addProperty("command", "startAi");

                if (preferredComandFilename != null) {
                    requestJson.addProperty("command_file_out", preferredComandFilename);
                }

                if (preferredStartFilename != null) {
                    requestJson.addProperty("start_file", preferredStartFilename);
                }

                out.writeUTF(requestJson.toString());
            } catch (SocketTimeoutException e) {
                System.err.println("Failed on connect timeout");
                socket.close();
            }

            DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                    .getInputStream()));

            socket.setSoTimeout(5000);

            String readLine = in.readUTF();
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

        ParsedSaveState saveStateBefore = new ParsedSaveState(fileToString(startFileName));
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

    private static void runAllBattlesForSeed(String seed) throws IOException {
        String allSeedsDir = String.format("C:/stuff/rundata/runs/%s", seed);

        final int [] lastFloorVisited = new int[1];
        lastFloorVisited[0] = 0;
        Files.list(new File(allSeedsDir).toPath()).forEach(floorDirsInSeedPath -> {
            try {
                int previousFloor = lastFloorVisited[0];
                int currentFloor = Integer.parseInt(floorDirsInSeedPath.getFileName().toString());

                final String[] paths = new String[5];
                Files.list(floorDirsInSeedPath).forEach(filePath -> {
                    if (filePath.toString().endsWith("autosave")) {
                        paths[0] = filePath.toString();
                        paths[1] = filePath.getFileName().toString();
                    } else if (filePath.toString().endsWith("start.txt")) {
                        paths[2] = filePath.toString();
                    } else if (filePath.toString().endsWith("end.txt")) {
                        paths[3] = filePath.toString();
                    } else if (filePath.toString().endsWith("commands.txt")) {
                        paths[4] = filePath.toString();
                    }

                });
                if (paths[0] != null && paths[2] != null) {
                    String savePath = paths[0];
                    String playerClass = paths[1].split("\\.")[0];

                    System.err.println("found both " + savePath);

                    boolean hasCommandFile = false;//paths[3] != null && paths[4] != null;

                    ParsedSaveState startState = new ParsedSaveState(fileToString(paths[2]));

                    for (String id : startState.getRelicIds()) {
                        if (FORBIDDEN_RELIC_IDS.contains(id)) {
                            return;
                        }
                    }


                    boolean isVanilla = playerClass.equals("DEFECT") ||
                            playerClass.equals("THE_SILENT") ||
                            playerClass.equals("IRONCLAD") ||
                            playerClass.equals("RobotSpaceExplorer") ||
                            playerClass.equals("WATCHER");

                    if (!isVanilla) {
                        return;
                    }

                    if (!hasCommandFile) {
                        // No Mod Characters at this point
                        fightSaveFile(paths[0], playerClass, previousFloor - 1, currentFloor - 1);
                        lastFloorVisited[0] = currentFloor;

                        Path filePath = Paths.get(paths[0]);

                        paths[3] = filePath.getParent() + "\\end.txt";
                        paths[4] = filePath.getParent() + "\\commands.txt";
                    }

                    JsonObject commandsJson = new JsonParser().parse(fileToString(paths[4]))
                                                              .getAsJsonObject();

                    int predictedHpLoss = commandsJson.has("predictor_damage") ? commandsJson.get("predictor_damage").getAsInt() : 0;

                    int startHp = startState.getCurrentHp();
                    int endHp = new ParsedSaveState(fileToString(paths[3])).getCurrentHp();
                    ParsedSaveState endState = new ParsedSaveState(fileToString(paths[3]));

                    int actualHpLoss = startHp - endHp;

                    RunResult result = new RunResult();

                    result.characterClass = playerClass;
                    result.fightName = endState.getFightName();
                    result.enemiesString = startState.getEnemiesString();

                    result.deckString = startState.getDeckString();
                    result.relicString = startState.getRelicString();
                    result.potionString = startState.getPotionString();

                    result.seed = seed;
                    result.floorNum = startState.getFloorNum();

                    result.predicatedDamage = predictedHpLoss;
                    result.actualDamage = actualHpLoss;
                    result.startingHp = startState.getCurrentHp();

                    result.ascensionLevel = startState.getAscension();

                    runResults.add(result);

                    System.err.println(String
                            .format("Loss : Predicted - %d : Actual - %d", predictedHpLoss, actualHpLoss));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
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

        int getAscension() {
            return saveStateJson.get("ascension_level").getAsInt();
        }

        int getMaxHp() {
            return playerCreatureJson.get("max_health").getAsInt();
        }

        ArrayList<String> getRelicIds() {
            ArrayList<String> result = new ArrayList<>();

            JsonArray relics = playerJson.get("relics").getAsJsonArray();
            for (JsonElement element : relics) {
                result.add(element.getAsJsonObject().get("relic_id").getAsString());
            }

            return result;
        }

        String getDeckString() {
            ArrayList<String> cardIds = new ArrayList<>();

            playerJson.get("master_deck").getAsJsonArray().forEach(relicJson -> cardIds
                    .add(relicJson.getAsJsonObject().get("card_id").getAsString()));

            return cardIds.stream().collect(Collectors.joining(","));
        }

        String getRelicString() {
            ArrayList<String> relicIds = new ArrayList<>();

            playerJson.get("relics").getAsJsonArray().forEach(relicJson -> relicIds
                    .add(relicJson.getAsJsonObject().get("relic_id").getAsString()));

            return relicIds.stream().collect(Collectors.joining(","));
        }

        String getPotionString() {
            ArrayList<String> potionids = new ArrayList<>();

            playerJson.get("potions").getAsJsonArray().forEach(relicJson -> potionids
                    .add(relicJson.getAsJsonObject().get("id").getAsString()));

            return potionids.stream().collect(Collectors.joining(","));
        }

        String getEnemiesString() {
            ArrayList<String> enemyNames = new ArrayList<>();

            saveStateJson.get("cur_map_node_state").getAsJsonObject().get("monster_data")
                         .getAsJsonArray()
                         .forEach(monsterEle -> enemyNames
                                 .add(monsterEle.getAsJsonObject().get("creature")
                                                .getAsJsonObject().get("name")
                                                .getAsString()));

            return enemyNames.stream().collect(Collectors.joining(","));
        }

        String getFightName() {
            return saveStateJson.get("lastCombatMetricKey").getAsString();
        }
    }

    private static String fileToString(String fileName) throws IOException {
        return Files.lines(Paths.get(fileName))
                    .collect(Collectors.joining());
    }
}

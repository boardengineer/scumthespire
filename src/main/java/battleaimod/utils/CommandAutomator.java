package battleaimod.utils;

import basemod.BaseMod;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.devcommands.ConsoleCommand;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CommandAutomator {

    private final static Queue<String> fightCommands = new ArrayDeque<>();

    private final static List<String> initCommands = new ArrayList<>();


    public static void runInitCommands(){
        for(String cmd:initCommands){
            runCommand(cmd);
        }
    }

    public static void readCommands(){
        System.out.println("reading commands from files...");
        readCommandsFromFile("InitCommands.txt", initCommands);
        readCommandsFromFile("FightCommands.txt", fightCommands);
    }

    public static void readCommandsEval(){
        System.out.println("reading commands from files...");
        readCommandsFromFile("InitCommands.txt", initCommands);
        readCommandsFromFile("EvalFightCommands.txt", fightCommands);
    }

    public static void restartCurrentFight(){
        System.out.println("Running fight command");
        runCommand(fightCommands.peek());
    }

    public static String getCurrentFight(){
        return fightCommands.peek();
    }

    public static boolean hasNextFight(){
        return !fightCommands.isEmpty();
    }

    public static void advanceNextFight(){
        //DOES NOT RUN THE FIGHT COMMAND
        fightCommands.poll();
    }

    // File reader logic
    public static void readCommandsFromFile(String fileName, Collection<String> collection) {
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("Could not find the command file at: " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            collection.clear();
            String line;
            System.out.println("--- Reading Batch Commands from " + fileName + " ---");

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }

                System.out.println("Reading: " + line);
                collection.add(line);
            }

            System.out.println("--- Finished Reading Commands ---");

        } catch (IOException e) {
            System.out.println("An error occurred while reading the command file!");
            e.printStackTrace();
        }
    }

    // Silent execution logic
    /**
     * Executes a BaseMod console command silently via code.
     */
    public static void runCommand(String commandString) {
        if (commandString == null || commandString.trim().isEmpty()) {
            return;
        }

        // Split the command into an array of words, exactly how the DevConsole does
        String[] tokens = commandString.trim().split(" ");

        try {
            // Pass the tokens directly into BaseMod's built-in execution method
            ConsoleCommand.execute(tokens);
            System.out.println("Ran command: " + commandString);
        } catch (Exception e) {
            System.out.println("Error executing command: " + commandString);
            e.printStackTrace();
        }
    }
}
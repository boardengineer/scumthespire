package battleaimod.utils;

import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class FightRequestClient {
    private static final String HOST_IP = "127.0.0.1";

    public static void main(String[] args) {
        String path = "C:\\stuff\\rundata\\runs\\T9R4D9U3ZY43\\27\\THE_SILENT.autosave";

        fightSaveFile(path, "THE_SILENT");
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
}

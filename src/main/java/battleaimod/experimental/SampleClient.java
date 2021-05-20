package battleaimod.experimental;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class SampleClient {
    private static final String HOST_IP = "127.0.0.1";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(HOST_IP, PORT));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    DataInputStream inStream = new DataInputStream(new BufferedInputStream(socket
                            .getInputStream()));

                    while(true) {
                        System.out.println(inStream.readUTF());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                out.writeUTF(in.nextLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

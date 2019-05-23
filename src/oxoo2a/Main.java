package oxoo2a;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void fatal ( String comment, Exception e ) {
        System.out.println(comment);
        if (e != null) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            fatal("Usage: cubes_server <port>",null);
        int port = Integer.parseInt(args[0]);

        listenThread = new Thread(() -> listen(port));
        listenThread.start();

        // Wait for user input to stop server and send terminate messages to all clients

    }

    // ************************************************************************
    // Server
    // ************************************************************************
    private static void listen ( int port ) {
        try {
            ServerSocket s = new ServerSocket(port);
            while (true) {
                Socket client = s.accept();
                ClientConnect cc = new ClientConnect(client, namedClients);
            }
        }
        catch (Exception e) {
            fatal("Exception while listening",e);
        }
    }

    private static Map<String,ClientConnect> namedClients = new HashMap<>();
    private static Thread listenThread;
}

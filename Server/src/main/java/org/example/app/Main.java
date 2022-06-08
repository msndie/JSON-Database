package org.example.app;

import org.example.server.Server;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final int port = 34522;

    public static void main( String[] args ) {
        File file = new File("db.json");
        try {
            if (file.createNewFile()) {
                System.out.println("File has been created");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Server server = new Server(port, file);
        server.start();
    }
}

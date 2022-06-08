package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import org.example.models.Request;

public class App {

    private static final int PORT = 34522;
    private static final String ADDRESS = "127.0.0.1";

    static class Args {

        @Parameter(names = "-t", description = "type")
        private String type;

        @Parameter(names = "-k", description = "key")
        private String key;

        @Parameter(names = "-v", description = "value")
        private String value;

        @Parameter(names = "-in", description = "file")
        private String file;
    }

    public static void main(String[] args) {
        Args args1 = new Args();
        JCommander.newBuilder()
                .addObject(args1)
                .build()
                .parse(args);
        if (args1.file == null && args1.key == null
                && args1.type == null && args1.value == null) {
            System.err.println("Wrong arguments");
            return;
        }
        System.out.println("Client started!");
        try (Socket socket = new Socket(ADDRESS, PORT);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            String json;
            if (args1.file == null) {
                Gson gson = new Gson();
                json = gson.toJson(new Request(args1.key, args1.value, args1.type));
            } else {
                json = new String(Files.readAllBytes(Paths.get(args1.file)));
            }
            System.out.println("Sent: " + json);
            output.writeUTF(json);
            String received = input.readUTF();
            System.out.printf("Received: %s\n", received);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

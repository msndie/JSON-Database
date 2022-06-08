package org.example.server;

import com.google.gson.*;
import org.example.models.Response;
import org.example.repositories.JsonRepository;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final Gson gson;
    private final JsonRepository repository;

    public Server(int port, File file) {
        this.port = port;
        gson = new GsonBuilder().setPrettyPrinting().create();
        repository = new JsonRepository(file);
    }

    public void start() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        ConcurrentLinkedDeque<Socket> sockets = new ConcurrentLinkedDeque<>();
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server started!");
            while (true) {
                Socket socket = server.accept();
                sockets.add(socket);
                JsonElement request = checkExit(socket);
                executor.submit(() -> {
                    Socket tmp = sockets.getLast();
                    try {
                        DataOutputStream output = new DataOutputStream(tmp.getOutputStream());
                        String sent;
                        if (request == null) {
                            sent = gson.toJson(new Response("OK", null, null));
                        } else {
                            sent = execCmd(request);
                        }
                        output.writeUTF(sent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sockets.remove(tmp);
                    try {
                        Thread.sleep(200);
                        tmp.close();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                if (request == null) {
                    break;
                }
            }
            executor.shutdown();
            sockets.forEach(s -> {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonElement checkExit(Socket socket) {
        JsonObject request;
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            String received = input.readUTF();
            request = gson.fromJson(received, JsonObject.class);
            if (request.get("type").getAsString().equals("exit")) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return JsonNull.INSTANCE;
        }
        return request;
    }

    private String execCmd(JsonElement request) {
        String ret;

        if (request.isJsonNull()) {
            return gson.toJson(new Response("ERROR", null, null));
        }
        if ("get".equals(request.getAsJsonObject().get("type").getAsString())) {
            ret = repository.get(request.getAsJsonObject());
        } else if ("set".equals(request.getAsJsonObject().get("type").getAsString())) {
            ret = repository.set(request.getAsJsonObject());
        } else if ("delete".equals(request.getAsJsonObject().get("type").getAsString())) {
            ret = repository.delete(request.getAsJsonObject());
        } else {
            ret = gson.toJson(new Response("ERROR", "UNKNOWN TYPE", null));
        }
        return ret;
    }
}
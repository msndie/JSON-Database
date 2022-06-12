package org.example.server;

import com.google.gson.*;
import org.example.models.Response;
import org.example.repositories.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private int port;
    private Gson gson;
    private JsonRepository repository;

    @Autowired
    @Qualifier("gson")
    private void setGson(Gson gson) {
        this.gson = gson;
    }

    @Autowired
    @Qualifier("getRep")
    private void setRepository(JsonRepository repository) {
        this.repository = repository;
    }

    @Autowired
    @Qualifier("getPort")
    private void setPort(int port) {
        this.port = port;
    }

    public Server() {
    }

    public int getPort() {
        return port;
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
                        String sent;
                        if (request == null) {
                            sent = gson.toJson(new Response("OK", null, null));
                        } else {
                            sent = execCmd(request);
                        }
                        DataOutputStream output = new DataOutputStream(tmp.getOutputStream());
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
            while (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                System.out.println("Still waiting for all tasks to shut down");
            }
            sockets.forEach(s -> {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server is down");
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

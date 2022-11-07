package org.example.server;

import com.google.gson.*;
import org.example.models.Response;
import org.example.repositories.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.*;

@Component
public class Server {
    private final int port;
    private final Gson gson;
    private final JsonRepository repository;

    @Autowired
    public Server(int port, Gson gson, JsonRepository repository) {
        this.port = port;
        this.gson = gson;
        this.repository = repository;
    }

    public int getPort() {
        return port;
    }

    public void start() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        ConcurrentLinkedQueue<Socket> sockets = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Optional<JsonElement>> requests = new ConcurrentLinkedQueue<>();

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server started!");
            while (true) {
                Socket socket = server.accept();
                sockets.add(socket);
                JsonElement request = checkExit(socket);
                requests.add(Optional.ofNullable(request));
                executor.submit(new Task(requests, sockets, repository, gson));
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
                    if (!s.isClosed()) {
                        s.close();
                    }
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
}

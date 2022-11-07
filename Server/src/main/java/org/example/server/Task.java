package org.example.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.example.models.Response;
import org.example.repositories.JsonRepository;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Task implements Runnable {

    private final ConcurrentLinkedQueue<Socket> sockets;
    private final ConcurrentLinkedQueue<Optional<JsonElement>> requests;
    private final Gson gson;
    private final JsonRepository repository;

    public Task(ConcurrentLinkedQueue<Optional<JsonElement>> requests,
                ConcurrentLinkedQueue<Socket> sockets,
                JsonRepository repository,
                Gson gson) {
        this.sockets = sockets;
        this.requests = requests;
        this.gson = gson;
        this.repository = repository;
    }

    @Override
    public void run() {
        Socket tmp = sockets.poll();
        if (tmp == null) {
            return;
        }

        Optional<JsonElement> req = requests.poll();
        if (req == null) {
            return;
        }

        try {
            String sent;
            if (!req.isPresent()) {
                sent = gson.toJson(new Response("OK", null, null));
            } else {
                sent = execCmd(req.get());
            }
            DataOutputStream output = new DataOutputStream(tmp.getOutputStream());
            output.writeUTF(sent);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            tmp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

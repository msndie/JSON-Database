package org.example.repositories;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.example.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class JsonRepository {
    private final ReentrantReadWriteLock lock;
    private Gson gson;
    private File file;

    @Autowired
    @Qualifier("gson")
    private void setGson(Gson gson) {
        this.gson = gson;
    }

    @Autowired
    @Qualifier("getFile")
    private void setFile(File file) {
        this.file = file;
    }

    public JsonRepository() {
        lock = new ReentrantReadWriteLock();
    }

    private String writeToFile(JsonElement obj) {
        try {
            lock.writeLock().lock();
            FileWriter writer = new FileWriter(file);
            gson.toJson(obj, writer);
            writer.write("\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            lock.writeLock().unlock();
            System.exit(1);
        }
        lock.writeLock().unlock();
        return gson.toJson(new Response("OK", null, null));
    }

    private JsonElement readFromFile() {
        JsonElement obj = null;
        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            lock.readLock().lock();
            obj = gson.fromJson(reader, JsonElement.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return obj;
    }

    public String get(JsonObject request) {
        Response response;
        JsonElement obj;
        JsonElement element = null;
        JsonElement key = request.get("key");
        if (key == null) {
            return gson.toJson(new Response("ERROR", null, null));
        }

        obj = readFromFile();
        if (obj == null) {
            response = new Response("ERROR", "No such key", null);
        } else {
            if (key.isJsonArray()) {
                JsonArray keys = key.getAsJsonArray();
                for (JsonElement k : keys) {
                    if (obj.getAsJsonObject().has(k.getAsString())) {
                        element = obj.getAsJsonObject().get(k.getAsString());
                        obj = element;
                    } else {
                        return gson.toJson(new Response("ERROR", "No such key", null));
                    }
                }
            } else {
                element = obj.getAsJsonObject().get(key.getAsString());
            }
            if (element == null) {
                response = new Response("ERROR", "No such key", null);
            } else {
                response = new Response("OK", null, element);
            }
        }
        return gson.toJson(response);
    }

    public String set(JsonObject request) {
        JsonElement obj;
        JsonElement val = request.get("value");
        JsonElement key = request.get("key");
        if (val == null || key == null) {
            return gson.toJson(new Response("ERROR", null, null));
        }
        obj = readFromFile();

        if (obj == null) {
            obj = new JsonObject();
        }
        if (key.isJsonArray()) {
            JsonElement tmp = obj;
            JsonArray keys = key.getAsJsonArray();
            JsonElement k = null;
            for (int i = 0; i < keys.size(); i++) {
                k = keys.get(i);
                if (i + 1 != keys.size()) {
                    tmp = tmp.getAsJsonObject().get(k.getAsString());
                }
            }
            if (k != null && tmp.isJsonObject()
                    && tmp.getAsJsonObject().has(k.getAsString())) {
                tmp.getAsJsonObject().remove(k.getAsString());
                tmp.getAsJsonObject().add(k.getAsString(), val);
            } else if (k != null) {
                tmp.getAsJsonObject().add(k.getAsString(), val);
            } else {
                return gson.toJson(new Response("ERROR", null, null));
            }
//            for (JsonElement k : keys) {
//                if (tmp.getAsJsonObject().has(k.getAsString())) {
//                    if (tmp.getAsJsonObject().get(k.getAsString()).isJsonPrimitive()) {
//                        tmp.getAsJsonObject().remove(k.getAsString());
//                        tmp.getAsJsonObject().add(k.getAsString(), val);
//                        break;
//                    }
//                    tmp = tmp.getAsJsonObject().get(k.getAsString());
//                } else {
//                    tmp.getAsJsonObject().add(k.getAsString(), val);
//                    break;
//                }
//            }
        } else {
            obj.getAsJsonObject().add(key.getAsString(), val);
        }
        return writeToFile(obj);
    }

    public String delete(JsonObject request) {
        JsonElement obj;
        JsonElement key = request.get("key");
        if (key == null) {
            return gson.toJson(new Response("ERROR", null, null));
        }
        obj = readFromFile();

        if (obj == null) {
            return gson.toJson(new Response("ERROR", "No such key", null));
        }
        if (key.isJsonArray()) {
            JsonElement tmp = obj;
            JsonArray keys = key.getAsJsonArray();
            JsonElement k = null;
            for (int i = 0; i < keys.size(); i++) {
                k = keys.get(i);
                if (i + 1 != keys.size()) {
                    tmp = tmp.getAsJsonObject().get(k.getAsString());
                }
            }
            if (k != null && tmp.isJsonObject()
                    && tmp.getAsJsonObject().has(k.getAsString())) {
                tmp.getAsJsonObject().remove(k.getAsString());
            } else {
                return gson.toJson(new Response("ERROR", "No such key", null));
            }
        } else {
            if (obj.getAsJsonObject().has(key.getAsString())) {
                obj.getAsJsonObject().remove(key.getAsString());
            } else {
                return gson.toJson(new Response("ERROR", "No such key", null));
            }
        }
        return writeToFile(obj);
    }
}

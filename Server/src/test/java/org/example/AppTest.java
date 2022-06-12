package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.example.config.ServerApplicationConfig;
import org.example.server.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class AppTest {
    private static Thread serverThread;

    private static int port;

    @BeforeAll
    static void serverStart() {
        AnnotationConfigApplicationContext contex;
        contex = new AnnotationConfigApplicationContext(ServerApplicationConfig.class);
        Server server = contex.getBean(Server.class);
        port = server.getPort();
        serverThread = new Thread(server::start);
        serverThread.start();
    }

    @Test
    public void test1() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"set\",\"key\":\"person\",\"value\":{\"name\":\"Elon Musk\",\"car\":{\"model\":\"Tesla Roadster\",\"year\":\"2018\"},\"rocket\":{\"name\":\"Falcon 9\", \"launches\":\"87\"}}}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"OK\"\n" +
                "}", received);
    }

    @Test
    public void test2() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"get\",\"key\":[\"person\",\"name\"]}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"OK\",\n" +
                "  \"value\": \"Elon Musk\"\n" +
                "}", received);
    }

    @Test
    public void test3() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"get\",\"key\":[\"person\",\"nameTEST\"]}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"ERROR\",\n" +
                "  \"reason\": \"No such key\"\n" +
                "}", received);
    }

    @Test
    public void test4() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"get\",\"key\":[\"nameTEST\"]}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"ERROR\",\n" +
                "  \"reason\": \"No such key\"\n" +
                "}", received);
    }

    @Test
    public void test5() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"pull\",\"key\":[\"nameTEST\"]}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"ERROR\",\n" +
                "  \"reason\": \"UNKNOWN TYPE\"\n" +
                "}", received);
    }

    @Test
    public void test6() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"get\"}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"ERROR\"\n" +
                "}", received);
    }

    @Test
    public void test7() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"set\"}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"ERROR\"\n" +
                "}", received);
    }

    @Test
    public void test8() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"delete\"}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"ERROR\"\n" +
                "}", received);
    }

    @Test
    public void test9() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"delete\",\"key\":[\"person\",\"nameTEST\"]}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"ERROR\",\n" +
                "  \"reason\": \"No such key\"\n" +
                "}", received);
    }

    @Test
    public void test10() {
        String received = null;
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"get\", \"key\":[\"person\", \"cat\", \"model\"]}");
            received = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("{\n" +
                "  \"response\": \"ERROR\",\n" +
                "  \"reason\": \"No such key\"\n" +
                "}", received);
    }

    @AfterAll
    static void serverStop() throws InterruptedException {
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output.writeUTF("{\"type\":\"exit\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverThread.join();
    }
}

package org.example;

import org.example.config.ServerApplicationConfig;
import org.example.server.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testMultiConnection() {
        String[] expected = new String[4];
        String[] actual = new String[4];
        expected[0] = ("{\n" +
                "  \"response\": \"OK\",\n" +
                "  \"value\": \"Hello world!\"\n" +
                "}");
        expected[1] = ("{\n" +
                "  \"response\": \"OK\",\n" +
                "  \"value\": \"Elon Musk\"\n" +
                "}");
        expected[2] = ("{\n" +
                "  \"response\": \"ERROR\",\n" +
                "  \"reason\": \"No such key\"\n" +
                "}");
        expected[3] = ("{\n" +
                "  \"response\": \"OK\"\n" +
                "}");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.submit(() -> {
            String received = null;
            try (Socket socket1 = new Socket("127.0.0.1", port);
                 DataOutputStream output1 = new DataOutputStream(socket1.getOutputStream());
                 DataInputStream input1 = new DataInputStream(socket1.getInputStream())) {
                output1.writeUTF("{\"type\":\"get\", \"key\":\"1\"}");
                received = input1.readUTF();
                actual[0] = (received);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        executor.submit(() -> {
            String received = null;
            try (Socket socket2 = new Socket("127.0.0.1", port);
                 DataOutputStream output2 = new DataOutputStream(socket2.getOutputStream());
                 DataInputStream input2 = new DataInputStream(socket2.getInputStream())) {
                output2.writeUTF("{\"type\":\"get\",\"key\":[\"person\",\"name\"]}");
                received = input2.readUTF();
                actual[1] = (received);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        executor.submit(() -> {
            String received = null;
            try (Socket socket3 = new Socket("127.0.0.1", port);
                 DataOutputStream output3 = new DataOutputStream(socket3.getOutputStream());
                 DataInputStream input3 = new DataInputStream(socket3.getInputStream())) {
                output3.writeUTF("{\"type\":\"get\",\"key\":[\"person\",\"nameTEST\"]}");
                received = input3.readUTF();
                actual[2] = (received);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        executor.submit(() -> {
            String received = null;
            try (Socket socket1 = new Socket("127.0.0.1", port);
                 DataOutputStream output4 = new DataOutputStream(socket1.getOutputStream());
                 DataInputStream input4 = new DataInputStream(socket1.getInputStream())) {
                output4.writeUTF("{\"type\":\"set\",\"key\":\"person\",\"value\":{\"name\":\"Elon Musk\",\"car\":{\"model\":\"Tesla Roadster\",\"year\":\"2018\"},\"rocket\":{\"name\":\"Falcon 9\", \"launches\":\"87\"}}}");
                received = input4.readUTF();
                actual[3] = (received);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        executor.shutdown();
        try {
            while (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                System.out.println("Executor still running");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertArrayEquals(expected, actual);
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

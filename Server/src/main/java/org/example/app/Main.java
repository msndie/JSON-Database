package org.example.app;

import org.example.config.ServerApplicationConfig;
import org.example.server.Server;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;

public class Main {
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
        AnnotationConfigApplicationContext contex;
        contex = new AnnotationConfigApplicationContext(ServerApplicationConfig.class);
        Server server = contex.getBean(Server.class);
        server.start();
    }
}

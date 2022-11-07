package org.example.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.repositories.JsonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;

@Configuration
@ComponentScan("org.example")
@PropertySource("classpath:conf.properties")
public class ServerApplicationConfig {
	@Value("${port}")
	private int port;
	@Value("${file}")
	private String fileName;

	@Bean
	public File getFile() {
		return new File(fileName);
	}

	@Bean
	public int getPort() {
		return port;
	}

	@Bean
	public Gson gson() {
		return new GsonBuilder().setPrettyPrinting().create();
	}
}

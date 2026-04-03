package com.springSecurity.authify.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;

@Configuration
public class EnvConfig {

    @PostConstruct
    public void loadEnv() {
        try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
            br.lines().forEach(line -> {
                if (!line.startsWith("#") && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    System.setProperty(parts[0].trim(), parts[1].trim());
                }
            });
        } catch (Exception e) {
            System.out.println("No .env file found or error loading it");
        }
    }
}
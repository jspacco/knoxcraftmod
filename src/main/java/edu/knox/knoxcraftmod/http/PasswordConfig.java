package edu.knox.knoxcraftmod.http;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class PasswordConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path CONFIG_PATH = Paths.get("config/passwords.toml");

    public static Map<String, String> loadOrCreate() {
        if (!Files.exists(CONFIG_PATH)) {
            try {
                Files.createDirectories(CONFIG_PATH.getParent());
                List<String> defaultLines = List.of(
                    "# This is a sample user/password file.",
                    "# Add more entries below using the format:",
                    "# username = \"password\"",
                    "",
                    "[users]",
                    "test = \"foobar123\""
                );
                Files.write(CONFIG_PATH, defaultLines);
                LOGGER.info("Created default passwords.toml file at {}", CONFIG_PATH);
            } catch (IOException e) {
                LOGGER.error("Could not create default password config", e);
                return Map.of(); // fail gracefully
            }
        }

        try {
            TomlParseResult result = Toml.parse(CONFIG_PATH);
            if (result.hasErrors()) {
                LOGGER.error("Error parsing passwords.toml:");
                result.errors().forEach(err -> LOGGER.error(err.toString()));
                return Map.of();
            }

            if (!result.contains("users")) {
                LOGGER.warn("passwords.toml missing [users] section");
                return Map.of();
            }

            return result.getTable("users").toMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof String)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> (String) e.getValue()
                ));

        } catch (Exception e) {
            LOGGER.error("Failed to read passwords.toml", e);
            return Map.of();
        }
    }
}


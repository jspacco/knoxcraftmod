package edu.knox.knoxcraftmod.http;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.command.Instruction;
import edu.knox.knoxcraftmod.command.ToroProgram;
import edu.knox.knoxcraftmod.data.ToroProgramData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class HttpServerManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    //TODO: load from config file
    //TODO: best way to set up a config file for a new server
    private static final Map<String, String> USER_CREDENTIALS = Map.of(
        "dev", "foobar123",
        "student1", "hello123"
    );

    public static void start(MinecraftServer server) throws Exception {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/upload", exchange -> handleUpload(exchange, server));
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();
        System.out.println("HTTP server running on port 8080");
    }

    private static void handleUpload(HttpExchange exchange, MinecraftServer server) {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                // Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            UploadRequest body = GSON.fromJson(
                new InputStreamReader(exchange.getRequestBody()), UploadRequest.class);

            if (!USER_CREDENTIALS.containsKey(body.username) || 
                !USER_CREDENTIALS.get(body.username).equals(body.password)) {
                send(exchange, 403, "Invalid username or password");
                return;
            }

            // Store the program
            ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
            ToroProgramData data = ToroProgramData.get(level);

            ToroProgram program = new ToroProgram(body.programName, "Uploaded via HTTP", body.program);

            Optional<GameProfile> profileOpt = server.getProfileCache().get(body.username);
            if (profileOpt.isEmpty()) {
                LOGGER.warn("Unknown username: {}", body.username);
                return;
            }
            UUID uuid = profileOpt.get().getId();
            data.addProgram(uuid, program);

            send(exchange, 200, "Program uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            send(exchange, 500, "Server error");
        }
    }

    private static void send(HttpExchange exchange, int code, String message) {
        try {
            byte[] bytes = message.getBytes();
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner class for parsing request
    private static class UploadRequest {
        String username;
        String password;
        String programName;
        java.util.List<Instruction> program;
    }
}

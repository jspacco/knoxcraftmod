package edu.knox.knoxcraftmod.http;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.KnoxcraftConfig;
import edu.knox.knoxcraftmod.command.ToroProgram;
import edu.knox.knoxcraftmod.data.ToroProgramData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class HttpServerManager {
    private static HttpServer httpServer;

    private static boolean LOGIN_REQUIRED;
    private static int PORT;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();


    //TODO: load from config file
    //TODO: best way to set up a config file for a new server
    private static final Map<String, String> USER_CREDENTIALS = Map.of(
        "dev", "foobar123",
        "student1", "hello123"
    );

    public static void start(MinecraftServer server) throws Exception 
    {
        PORT = KnoxcraftConfig.HTTP_PORT;
        LOGIN_REQUIRED = KnoxcraftConfig.LOGIN_REQUIRED;

        if (httpServer != null) {
            LOGGER.warn("HTTP server already running!");
            return;
        }
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/upload", exchange -> handleUpload(exchange, server));
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();
        LOGGER.debug("HTTP server running on port {}}", PORT);
    }

    public static void stop() {
        if (httpServer != null) {
            LOGGER.debug("Shutting down HTTP server...");
            // 0 = no delay
            httpServer.stop(0); 
            httpServer = null;
        }
    }

    private static void handleUpload(HttpExchange exchange, MinecraftServer server) {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                // Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            //TODO: username is not the minecraft username
            String username = exchange.getRequestHeaders().getFirst("X-Username");
            String password = exchange.getRequestHeaders().getFirst("X-Password");
            
            if (LOGIN_REQUIRED && (username == null || password == null)) {
                send(exchange, 400, "Missing login credentials in headers");
                return;
            }

            if (LOGIN_REQUIRED && (!USER_CREDENTIALS.containsKey(username) || 
                !USER_CREDENTIALS.get(username).equals(password)))
            {
                send(exchange, 403, "Invalid username or password");
                return;
            }

            ToroProgram program = GSON.fromJson(
                new InputStreamReader(exchange.getRequestBody()), ToroProgram.class);

            LOGGER.trace("Program: {}", program);

            // Store the program
            ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
            ToroProgramData data = ToroProgramData.get(level);

            // add the program with the username
            data.addProgram(username, program);

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

}

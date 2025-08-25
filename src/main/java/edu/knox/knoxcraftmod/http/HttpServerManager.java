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
import edu.knox.knoxcraftmod.command.ParallelTerpProgram;
import edu.knox.knoxcraftmod.command.SerialTerpProgram;
import edu.knox.knoxcraftmod.command.TerpProgram;
import edu.knox.knoxcraftmod.data.TerpProgramData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class HttpServerManager {
    private static HttpServer httpServer;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    // to be read from KnoxcraftConfig
    private static boolean LOGIN_REQUIRED;
    private static int PORT;

    // will be read from PasswordConfig
    private static Map<String, String> USER_CREDENTIALS;

    private static final String ALLOWED_ORIGIN = "http://localhost:8000"; // your Blockly page origin

    private static void addCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        exchange.getResponseHeaders().add("Vary", "Origin");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers",
            "Content-Type, X-Username, X-Password, X-MinecraftPlayername, X-Type");
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "600");
        // If you ever use cookies/Authorization bearer and need credentials:
        // exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
    }


    public static void start(MinecraftServer server) throws Exception 
    {
        PORT = KnoxcraftConfig.HTTP_PORT;
        LOGIN_REQUIRED = KnoxcraftConfig.LOGIN_REQUIRED;
        LOGGER.debug("Server login required is {}", LOGIN_REQUIRED);
        
        // load user credentials
        USER_CREDENTIALS = PasswordConfig.loadOrCreate();


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

            // Preflight: reply early
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                addCORS(exchange);
                exchange.sendResponseHeaders(204, -1); // no body
                exchange.close();
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                // Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String username = exchange.getRequestHeaders().getFirst("X-Username");
            String password = exchange.getRequestHeaders().getFirst("X-Password");
            String minecraftPlayername = exchange.getRequestHeaders().getFirst("X-MinecraftPlayername");
            String type = exchange.getRequestHeaders().getFirst("X-Type");
            
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

            // Store the program
            ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
            TerpProgramData data = TerpProgramData.get(level);

            if (!type.equals("serial") && !type.equals("parallel")) {
                LOGGER.error("Unknown type "+type);
                throw new RuntimeException("Unknown type (not serial or parallel): "+type);
            }
            TerpProgram program = null;
            if (type.equals("serial")) {
                String payload = new String(exchange.getRequestBody().readAllBytes());
                LOGGER.debug("Serial program JSON payload: {}", payload);
                program = GSON.fromJson(payload, SerialTerpProgram.class);
                LOGGER.trace("Serial Program uploaded: {}", program);
                // add the program with the username
            } else if (type.equals("parallel")) {
                program = GSON.fromJson(
                    new InputStreamReader(exchange.getRequestBody()), ParallelTerpProgram.class);
                    LOGGER.trace("Parallel Program uploaded: {}", program);
                
            }
            data.addProgram(minecraftPlayername, program);
            send(exchange, 200, "Program uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            send(exchange, 500, "Server error");
        }
    }

    private static void send(HttpExchange exchange, int code, String message) {
        try {
            addCORS(exchange);
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

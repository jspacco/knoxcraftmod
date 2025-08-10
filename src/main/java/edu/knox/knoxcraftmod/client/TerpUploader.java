package edu.knox.knoxcraftmod.client;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Client uploader.
 * 
 * Create an HTTP client and send a POST request to the server
 * with the program delivered as a JSON payload.
 * 
 * Basically, the Terp client generates a series of instructions
 * that are serialized into JSON and uploaded. The server
 * never runs the student code.
 */
public class TerpUploader
{
    private static final Gson GSON;
    static {
        // NOTE: we could refactor everything out of the static initializer
        // to chain together the calls, but I think this code is clearer
        //
        // custom serializers so that enums serialize correctly
        // TURN_LEFT becomes turnleft
        // SUGAR_CANE becomes minecraft:sugar_cane
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TerpCommand.class, new JsonSerializer<TerpCommand>() {
            @Override
            public JsonElement serialize(TerpCommand src, Type typeOfSrc, JsonSerializationContext context) {
               return new JsonPrimitive(src.getId());
            }
        });
        gsonBuilder.registerTypeAdapter(TerpBlockType.class, new JsonSerializer<TerpBlockType>() {
            @Override
            public JsonElement serialize(TerpBlockType src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.getId());
            }
        });
        GSON = gsonBuilder.create();
    }

    public static void upload(String serverUrl, 
        Terp terp,
        String minecraftPlayername,
        String username, String password)
    {
        Map<String, Object> json = Map.of(
            "programName", terp.getProgramName(),
            "description", terp.getDescription(),
            "instructions", terp.getInstructions());
        
        String jsonPayload = GSON.toJson(json);

        upload(serverUrl, "serial", jsonPayload, 
            minecraftPlayername,
            username, password);
    }

    public static void upload(String serverUrl, 
        ParallelTerp terp,
        String minecraftPlayername,
        String username, String password)
    {
        Map<String, Object> json = Map.of(
            "programName", terp.getProgramName(),
            "description", terp.getDescription(),
            "threads", terp.getAllThreads());
        
        String jsonPayload = GSON.toJson(json);

        upload(serverUrl, "parallel", jsonPayload, 
            minecraftPlayername,
            username, password);
    }

    private static void upload(String serverUrl, 
        String type, String json,
        String minecraftPlayername,
        String username, String password)
    {
        HttpClient client = HttpClient.newHttpClient();
        System.out.println(json);
        // Send the POST request to the server
        // Handle the response and any errors
        // sending username and password as custom headers
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(serverUrl + "/upload"))
            .header("Content-Type", "application/json")
            .header("X-MinecraftPlayername", minecraftPlayername)
            .header("X-Username", username)
            .header("X-Password", password)
            .header("X-Type", type)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Upload Successful");
            } else {
                System.out.println("Upload failed " +response.body());
            }
        } catch (Exception e) {
            System.out.println("Upload failed with exception! " +e.toString());
        }
    }

}

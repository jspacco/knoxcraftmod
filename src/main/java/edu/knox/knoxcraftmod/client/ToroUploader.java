package edu.knox.knoxcraftmod.client;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ToroUploader
{
    // client upload code
    // create an HTTP client and send a POST request to the server
    // with the program data in JSON format
    private static final Gson GSON;
    static {
        //TODO: refactor with anonymous inner classes and chaining
        // custom serializers so that enums serialize correctly
        // TURN_LEFT becomes turnLeft
        // SUGAR_CANE becomes minecraft:sugar_cane
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ToroCommand.class, new ToroCommandSerializer());
        gsonBuilder.registerTypeAdapter(ToroBlockType.class, new ToroBlockTypeSerializer());
        GSON = gsonBuilder.create();
    }

    private HttpClient httpClient;
    private String serverUrl;
    private String username;
    private String password;

    public ToroUploader(String serverUrl, String username, String password) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.httpClient = HttpClient.newHttpClient();
    }

    

    public void uploadProgram(Toro toro)
    {
        String jsonPayload = GSON.toJson(toro);
        System.out.println(jsonPayload);
        // Send the POST request to the server
        // Handle the response and any errors
        // This is where you would implement the actual HTTP request logic

        // For example, using HttpClient to send the request

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(serverUrl + "/upload"))
            .header("Content-Type", "application/json")
            .header("X-Username", username)
            .header("X-Password", password)
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Upload Successful");
            } else {
                System.out.println("Upload failed " +response.body());
            }
        } catch (Exception e) {
            System.out.println("Upload failed with exception! " +e.toString());
        }

    }

    private static class ToroCommandSerializer implements JsonSerializer<ToroCommand> {
        @Override
        public JsonElement serialize(ToroCommand src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getId());
        }
    }

    private static class ToroBlockTypeSerializer implements JsonSerializer<ToroBlockType> {
        @Override
        public JsonElement serialize(ToroBlockType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getId());
        }

    }
}

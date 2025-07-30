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

/**
 * Client uploader.
 * 
 * Create an HTTP client and send a POST request to the server
 * with the program delivered as a JSON payload.
 * 
 * Basically, the Toro client generates a series of instructions
 * that are serialized into JSON and uploaded. The server
 * never runs the student code.
 */
public class ToroUploader
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
        gsonBuilder.registerTypeAdapter(ToroCommand.class, new JsonSerializer<ToroCommand>() {
            @Override
            public JsonElement serialize(ToroCommand src, Type typeOfSrc, JsonSerializationContext context) {
               return new JsonPrimitive(src.getId());
            }
        });
        gsonBuilder.registerTypeAdapter(ToroBlockType.class, new JsonSerializer<ToroBlockType>() {
            @Override
            public JsonElement serialize(ToroBlockType src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.getId());
            }
        });
        GSON = gsonBuilder.create();
    }

    private HttpClient httpClient;
    private String serverUrl;
    private String username;
    private String password;
    private String minecraftPlayername;

    /**
     * Create a new Toro that will upload to the given server URL
     * for the given minecraft player name. 
     * 
     * The minecraft player name is <b>NOT</b> case sensitive.
     * 
     * The serverUrl should be given to you by your instructor.
     * 
     * <b>NOTE</b> This constructor assumes no authentication
     * will be required by the server; ask your instrutor about this.
     * @param serverUrl
     * @param minecraftPlayername
     */
    public ToroUploader(String serverUrl, String minecraftPlayername) {
        this(serverUrl, minecraftPlayername, "", "");
    }

    /**
     * Create a new Toro that will upload to the given server URL
     * for the given minecraft player name, using the 
     * given username and password for authentication.
     * 
     * The minecraft player name is <b>NOT</b> case sensitive.
     * 
     * The serverUrl will be given to you by your instructor.
     * 
     * The username and password will be given to you by your instructor.
     * 
     * <b>DO NOT USE YOUR PASSWORD FOR YOUR COLLEGE EMAIL ADDRESS</b>.
     * This is a different password only used for Knoxcraft.
     * @param serverUrl
     * @param minecraftPlayername
     * @param username
     * @param password
     */
    public ToroUploader(String serverUrl, String minecraftPlayername, String username, String password) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.minecraftPlayername = minecraftPlayername;
        this.httpClient = HttpClient.newHttpClient();
    }

    

    /**
     * Upload the Toro program to the minecraft server.
     * 
     * The upload goes to the serverUrl passed into the
     * constructor, and will assign the Toro to the 
     * player with the minecraftPlayername given to the
     * constructor.
     * 
     * The program is converted to JSON and transmitted
     * to the server as a String. No student code actually 
     * runs on the Minecraft server.
     * 
     * @param toro
     */
    public void uploadProgram(Toro toro)
    {
        String jsonPayload = GSON.toJson(toro);
        System.out.println(jsonPayload);
        // Send the POST request to the server
        // Handle the response and any errors
        // sending username and password as custom headers
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(serverUrl + "/upload"))
            .header("Content-Type", "application/json")
            .header("X-MinecraftPlayername", minecraftPlayername)
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
}

package edu.knox.knoxcraftmod.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class Uploader
{
    // client upload code
    // create an HTTP client and send a POST request to the server
    // with the program data in JSON format

    private HttpClient httpClient;
    private String serverUrl;
    private String username;
    private String password;

    public Uploader(String serverUrl, String username, String password) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void uploadProgram(String programName, String description, String programData) {
        // Create the JSON payload
        String jsonPayload = String.format(
            "{\"username\":\"%s\", \"password\":\"%s\", \"programName\":\"%s\", \"description\":\"%s\", \"program\":%s}",
            username, password, programName, description, programData);

        // Send the POST request to the server
        // Handle the response and any errors
        // This is where you would implement the actual HTTP request logic

        // For example, using HttpClient to send the request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(serverUrl + "/upload"))
            .header("Content-Type", "application/json")
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

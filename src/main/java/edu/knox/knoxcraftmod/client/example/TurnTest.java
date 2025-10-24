package edu.knox.knoxcraftmod.client.example;

import edu.knox.knoxcraftmod.client.*;

public class TurnTest {
    public static void main(String[] args) 
    {
        
        String serverUrl = "http://54.226.75.11:8080";
        String minecraftPlayername = "spacdog";
        String username = "test";
        String password = "foobar123";
        String programName = "turntest";
        String description = "Practice turning";

        Terp terp = new Terp(programName, description);

        terp.turnRight();

        TerpUploader.upload(serverUrl, terp, minecraftPlayername, username, password);

    }
    
}

package edu.knox.knoxcraftmod.client;

import edu.knox.knoxcraftmod.client.*;

public class ToroClientExample 
{

    public static void main(String[] args) {
        // Example usage of the Uploader class
        String serverUrl = "http://localhost:8080";
        // TODO: your username on minecraft
        String minecraftPlayername = "dev";
        // TODO: your college email username
        String username = "test";
        // TODO: the password provided for you by your instructor
        String password = "foobar123";
        //ToroUploader uploader = new ToroUploader(serverUrl,minecraftPlayername, username, password);
        
        String programName = "flump";
        String description = "This is an example program.";

        Toro toro = new Toro(programName, description);
        
        for (int i=0; i<3; i++) {
            toro.forward();
            toro.setBlock(ToroBlockType.DIRT);
        }

        ToroUploader.upload(serverUrl, toro, minecraftPlayername, username, password);
    }
}


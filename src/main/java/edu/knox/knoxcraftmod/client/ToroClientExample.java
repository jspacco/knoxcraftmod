package edu.knox.knoxcraftmod.client;

import com.google.gson.Gson;

public class ToroClientExample 
{

    public static void main(String[] args) {
        // Example usage of the Uploader class
        String serverUrl = "http://localhost:8080";
        String username = "dev";
        String password = "foobar123";
        ToroUploader uploader = new ToroUploader(serverUrl, username, password);
        
        //String programName = "test2";
        String programName = "flump";
        String description = "This is an example program.";

        Toro toro = new Toro(programName, description);
        for (int i=0; i<5; i++) {
            toro.down();
            toro.setBlock(ToroBlockType.AIR);
        }
        for (int i=0; i<10; i++) {
            toro.forward();
            toro.setBlock(ToroBlockType.DIRT);
        }

        uploader.uploadProgram(toro);
    }
}


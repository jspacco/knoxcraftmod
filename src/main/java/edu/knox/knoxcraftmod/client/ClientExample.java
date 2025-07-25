package edu.knox.knoxcraftmod.client;

public class ClientExample 
{

    public static void main(String[] args) {
        // Example usage of the Uploader class
        String serverUrl = "http://localhost:8080";
        String username = "dev";
        String password = "foobar123";
        Uploader uploader = new Uploader(serverUrl, username, password);
        //String programName = "test2";
        String programName = "flump";
        String description = "This is an example program.";
        // Example JSON data
        String programData = "[{\"command\":\"up\",\"blockType\":\"stone\"}]"; 
        uploader.uploadProgram(programName, description, programData);
    }
}


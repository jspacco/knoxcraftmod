package edu.knox.knoxcraftmod.client;

public class PyramidExample {
    public static void main(String[] args)
    {
        // Example usage of the Uploader class
        String serverUrl = "http://localhost:8080";
        // TODO: your username on minecraft
        String minecraftPlayername = "dev";
        // TODO: your college email username
        String username = "test";
        // TODO: the password provided for you by your instructor
        String password = "foobar123";
        ToroUploader uploader = new ToroUploader(serverUrl,minecraftPlayername, username, password);
        
        String programName = "pyramid";
        String description = "Draw a pyramid.";

        Toro toro = new Toro(programName, description);
        
        for (int base=8; base>=0; base-=2){
            for (int i=0; i<base; i++) {
                for (int j=0; j<base; j++) {
                    toro.forward();
                    toro.setBlock(ToroBlockType.OBSIDIAN);
                }
                for (int j=0; j<base; j++) {
                    toro.back();
                }
                toro.right();
            }
            for (int i=0; i<base; i++) {
                toro.left();
            }
            toro.forward();
            toro.right();
            toro.up();
        }

        uploader.uploadProgram(toro);
    }
    
}

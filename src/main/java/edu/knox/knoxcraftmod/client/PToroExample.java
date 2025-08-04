package edu.knox.knoxcraftmod.client;

import edu.knox.knoxcraftmod.client.*;

public class PToroExample 
{

    public static void main(String[] args)
    {
        String programName = "parallelpyramid";
        String description = "Draw a pyramid. In Parallel!";
        String serverUrl = "http://localhost:8080";
        String minecraftPlayer = "dev";
        String username = "test";
        String password = "foobar123";
        
        ParallelToro toro = new ParallelToro(programName, description);

        // first thread
        toro.addThread(t -> {
            int base = 8;
            for (int i=0; i<base; i++) {
                for (int j=0; j<base; j++) {
                    t.forward();
                    t.setBlock(ToroBlockType.OBSIDIAN);
                }
                for (int j=0; j<base; j++) {
                    t.back();
                }
                t.right();
            }
        });
        // second thread
        toro.addThread(t -> {
            t.forward();
            t.up();
            t.right();
            int base = 6;
            for (int i=0; i<base; i++) {
                for (int j=0; j<base; j++) {
                    t.forward();
                    t.setBlock(ToroBlockType.OBSIDIAN);
                }
                for (int j=0; j<base; j++) {
                    t.back();
                }
                t.right();
            }
        });
        // upload to server
        ToroUploader.upload(serverUrl, toro, minecraftPlayer, username, password);
    }
    
}

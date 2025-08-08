package edu.knox.knoxcraftmod.client;

import edu.knox.knoxcraftmod.client.*;

public class PMauritius {
    public static void main(String[] args)
    {
        String serverUrl = "http://localhost:8080";
        String minecraftPlayername = "dev";
        String username = "test";
        String password = "foobar123";
        String programName = "pflag";
        String description = "Mauritius in parallel!";

        ParallelTerp terp = new ParallelTerp(programName, description);
        
        int length = 12;
        int width = 4;
        terp.addThread(t -> {
            for (int i=0; i<width*3; i++) t.nop();

            for (int i=0; i<width; i++) {
                for (int j=0; j<length; j++) {
                    t.forward();
                    t.setBlock(TerpBlockType.RED_WOOL);
                }
                for (int j=0; j<length; j++) {
                    t.back();
                }
                t.right();
            }
            
        });
        terp.addThread(t -> {
            for (int i=0; i<width; i++) t.right();
            for (int i=0; i<width*2; i++) t.nop();

            for (int i=0; i<width; i++) {
                for (int j=0; j<length; j++) {
                    t.forward();
                    t.setBlock(TerpBlockType.BLUE_WOOL);
                }
                for (int j=0; j<length; j++) {
                    t.back();
                }
                t.right();
            }
        });

        terp.addThread(t -> {
            for (int i=0; i<width*2; i++) t.right();
            for (int i=0; i<width; i++) t.nop();

            for (int i=0; i<width; i++) {
                for (int j=0; j<length; j++) {
                    t.forward();
                    t.setBlock(TerpBlockType.YELLOW_WOOL);
                }
                for (int j=0; j<length; j++) {
                    t.back();
                }
                t.right();
            }
        });

        terp.addThread(t -> {
            for (int i=0; i<width*3; i++) t.right();

            for (int i=0; i<width; i++) {
                for (int j=0; j<length; j++) {
                    t.forward();
                    t.setBlock(TerpBlockType.GREEN_WOOL);
                }
                for (int j=0; j<length; j++) {
                    t.back();
                }
                t.right();
            }
        });

        TerpUploader.upload(serverUrl, terp, minecraftPlayername, username, password);
    }
    
}

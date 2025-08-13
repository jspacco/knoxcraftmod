package edu.knox.knoxcraftmod.client.example;

import edu.knox.knoxcraftmod.client.*;

public class Mauritius {

    public static void main(String[] args)
    {
        String serverUrl = "http://localhost:8080";
        String minecraftPlayername = "spacdog";
        String username = "test";
        String password = "foobar123";
        String programName = "flag";
        String description = "Flag of Mauritius";

        Terp terp = new Terp(programName, description);

        int length = 12;
        int width = 4;
        for (int i=0; i<width*4; i++) {
            for (int j=0; j<length; j++) {
                terp.forward();
                if (i / 4 == 0)
                    terp.setBlock(TerpBlockType.RED_WOOL);
                else if (i / 4 == 1)
                    terp.setBlock(TerpBlockType.BLUE_WOOL);
                else if (i / 4 == 2)
                    terp.setBlock(TerpBlockType.YELLOW_WOOL);
                else
                    terp.setBlock(TerpBlockType.GREEN_WOOL);
            }
            for (int j=0; j<length; j++) terp.back();
            terp.right();
        }


        TerpUploader.upload(serverUrl, terp, minecraftPlayername, username, password);
    }
    
}

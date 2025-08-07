package edu.knox.knoxcraftmod.client;

import edu.knox.knoxcraftmod.client.*;

public class Mauritius {

    public static void main(String[] args)
    {
        String serverUrl = "http://localhost:8080";
        String minecraftPlayername = "dev";
        String username = "test";
        String password = "foobar123";
        String programName = "flag";
        String description = "Flag of Mauritius";

        Toro toro = new Toro(programName, description);

        int length = 12;
        int width = 4;
        for (int i=0; i<width*4; i++) {
            for (int j=0; j<length; j++) {
                toro.forward();
                if (i / 4 == 0)
                    toro.setBlock(ToroBlockType.RED_WOOL);
                else if (i / 4 == 1)
                    toro.setBlock(ToroBlockType.BLUE_WOOL);
                else if (i / 4 == 2)
                    toro.setBlock(ToroBlockType.YELLOW_WOOL);
                else
                    toro.setBlock(ToroBlockType.GREEN_WOOL);
            }
            for (int j=0; j<length; j++) toro.back();
            toro.right();
        }


        ToroUploader.upload(serverUrl, toro, minecraftPlayername, username, password);
    }
    
}

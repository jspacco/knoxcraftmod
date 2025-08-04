package edu.knox.knoxcraftmod.client;

import java.util.LinkedList;

public class Toro extends AbstractToro
{
    private final String programName;
    private final String description;

    /**
     * Create a Toro with the given name and description.
     * 
     * @param programName
     * @param description
     */
    public Toro(String programName, String description)
    {
        super(new LinkedList<ToroInstruction>());
        if (programName == null || description == null) {
            throw new IllegalArgumentException("programName and description cannot be null!");
        }
        this.programName = programName;
        this.description = description;
    }

    public String getProgramName() {
        return programName;
    }

    public String getDescription() {
        return description;
    }

}

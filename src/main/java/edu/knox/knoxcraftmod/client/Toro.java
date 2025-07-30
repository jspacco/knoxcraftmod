package edu.knox.knoxcraftmod.client;

import java.util.LinkedList;
import java.util.List;

public class Toro
{
    @SuppressWarnings("unused")
    private final String programName;
    @SuppressWarnings("unused")
    private final String description;
    private List<ToroInstruction> instructions = new LinkedList<>();

    /**
     * Create a Toro with the given name and description.
     * 
     * @param programName
     * @param description
     */
    public Toro(String programName, String description)
    {
        if (programName == null || description == null) {
            throw new IllegalArgumentException("programName and description cannot be null!");
        }
        this.programName = programName;
        this.description = description;
    }

    /**
     * Set the given block type at the Toro's current location.
     * 
     * @param toroBlockType
     */
    public void setBlock(ToroBlockType toroBlockType) {
        instructions.add(new ToroInstruction(ToroCommand.SET_BLOCK, toroBlockType));
    }

    /**
     * Move the Toro forward one block.
     */
    public void forward() {
        add(ToroCommand.FORWARD);
    }

    /**
     * Move the Toro back one block.
     */
    public void back() {
        add(ToroCommand.BACK);
    }

    /**
     * Turn the Toro left. The Toro will stay at the current block.
     */
    public void turnLeft() {
        add(ToroCommand.TURN_LEFT);
    }

    /**
     * Turn the Toro right. The Toro wil stay at the current block.
     */
    public void turnRight() {
        add(ToroCommand.TURN_RIGHT);
    }

    /**
     * Move the Toro one block to the left.
     */
    public void left() {
        add(ToroCommand.LEFT);
    }

    /**
     * Move the Toro one block to the right.
     */
    public void right() {
        add(ToroCommand.RIGHT);
    }

    /**
     * Move the Toro up one block.
     * 
     * The Toro cannot move up above the max height of the server.
     */
    public void up() {
        add(ToroCommand.UP);
    }

    /**
     * Move the Toro down one block. 
     * 
     * The Toro cannot move below the ground level,
     * which is -60 on modern Minecraft servers.
     * 
     */
    public void down() {
        add(ToroCommand.DOWN);
    }

    private void add(ToroCommand cmd) {
        instructions.add(new ToroInstruction(cmd));
    }

    private static class ToroInstruction 
    {
        @SuppressWarnings("unused")
        private final ToroCommand command;
        @SuppressWarnings("unused")
        private final ToroBlockType blockType;

        ToroInstruction(ToroCommand command, ToroBlockType toroBlockType) {
            this.command = command;
            this.blockType = toroBlockType;
        }

        ToroInstruction(ToroCommand command) {
            this(command, null);
        }
    }

}

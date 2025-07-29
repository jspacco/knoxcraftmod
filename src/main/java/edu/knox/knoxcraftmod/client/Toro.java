package edu.knox.knoxcraftmod.client;

import java.util.LinkedList;
import java.util.List;

import static edu.knox.knoxcraftmod.client.ToroCommand.*;


public class Toro
{
    @SuppressWarnings("unused")
    private final String programName;
    @SuppressWarnings("unused")
    private final String description;
    private List<ToroInstruction> instructions = new LinkedList<>();

    public Toro(String programName, String description)
    {
        if (programName == null || description == null) {
            throw new IllegalArgumentException("programName and description cannot be null!");
        }
        this.programName = programName;
        this.description = description;
    }

    public void setBlock(ToroBlockType toroBlockType) {
        instructions.add(new ToroInstruction(SET_BLOCK, toroBlockType));
    }

    public void forward() {
        add(FORWARD);
    }

    public void back() {
        add(BACK);
    }

    public void turnLeft() {
        add(TURN_LEFT);
    }

    public void turnRight() {
        add(TURN_RIGHT);
    }

    public void left() {
        add(LEFT);
    }

    public void right() {
        add(RIGHT);
    }

    public void up() {
        add(UP);
    }

    public void down() {
        add(DOWN);
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

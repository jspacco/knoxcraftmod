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

    public Toro(String programName, String description)
    {
        if (programName == null || description == null) {
            throw new IllegalArgumentException("programName and description cannot be null!");
        }
        this.programName = programName;
        this.description = description;
    }

    public void setBlock(ToroBlockType toroBlockType) {
        instructions.add(new ToroInstruction(ToroCommand.SET_BLOCK, toroBlockType));
    }

    public void forward() {
        add(ToroCommand.FORWARD);
    }

    public void back() {
        add(ToroCommand.BACK);
    }

    public void turnLeft() {
        add(ToroCommand.TURN_LEFT);
    }

    public void turnRight() {
        add(ToroCommand.TURN_RIGHT);
    }

    public void left() {
        add(ToroCommand.LEFT);
    }

    public void right() {
        add(ToroCommand.RIGHT);
    }

    public void up() {
        add(ToroCommand.UP);
    }

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

package edu.knox.knoxcraftmod.client;

import java.util.List;

public abstract class AbstractToro {

    protected List<ToroInstruction> instructions;

    public AbstractToro(List<ToroInstruction> instructions) {
        this.instructions = instructions;
    }

    protected List<ToroInstruction> getInstructions() {
        return this.instructions;
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

    protected void add(ToroCommand cmd) {
        instructions.add(new ToroInstruction(cmd));
    }
}

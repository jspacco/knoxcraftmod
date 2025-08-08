package edu.knox.knoxcraftmod.client;

import java.util.List;

public abstract class AbstractTerp {

    protected List<TerpInstruction> instructions;

    public AbstractTerp(List<TerpInstruction> instructions) {
        this.instructions = instructions;
    }

    protected List<TerpInstruction> getInstructions() {
        return this.instructions;
    }
    
    /**
     * Set the given block type at the Terp's current location.
     * 
     * @param terpBlockType
     */
    public void setBlock(TerpBlockType terpBlockType) {
        instructions.add(new TerpInstruction(TerpCommand.SET_BLOCK, terpBlockType));
    }

    /**
     * Move the Terp forward one block.
     */
    public void forward() {
        add(TerpCommand.FORWARD);
    }

    /**
     * Move the Terp back one block.
     */
    public void back() {
        add(TerpCommand.BACK);
    }

    /**
     * Turn the Terp left. The Terp will stay at the current block.
     */
    public void turnLeft() {
        add(TerpCommand.TURN_LEFT);
    }

    /**
     * Turn the Terp right. The Terp wil stay at the current block.
     */
    public void turnRight() {
        add(TerpCommand.TURN_RIGHT);
    }

    /**
     * Move the Terp one block to the left.
     */
    public void left() {
        add(TerpCommand.LEFT);
    }

    /**
     * Move the Terp one block to the right.
     */
    public void right() {
        add(TerpCommand.RIGHT);
    }

    /**
     * Move the Terp up one block.
     * 
     * The Terp cannot move up above the max height of the server.
     */
    public void up() {
        add(TerpCommand.UP);
    }

    /**
     * Move the Terp down one block. 
     * 
     * The Terp cannot move below the ground level,
     * which is -60 on modern Minecraft servers.
     * 
     */
    public void down() {
        add(TerpCommand.DOWN);
    }

    /**
     * No operation. The Terp sits there for 1 tick and does nothing.
     * 
     * Nops allow threads to be paused should parallel programs want to line up
     * and synchronize their threads.
     */
    public void nop() {
        add(TerpCommand.NOP);
    }

    protected void add(TerpCommand cmd) {
        instructions.add(new TerpInstruction(cmd));
    }
}

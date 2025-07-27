package edu.knox.knoxcraftmod.command;

import net.minecraft.nbt.CompoundTag;

public class Instruction {
    public final String command;
    public final String blockType;

    public Instruction(String command, String blockType) {
        this.command = command;
        this.blockType = blockType;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("cmd", command);
        if (blockType != null){
            tag.putString("blk", blockType);
        }
        return tag;
    }

    public static Instruction fromNBT(CompoundTag tag) {
        String cmd = tag.getString("cmd");
        String block = tag.contains("blk") ? tag.getString("blk") : null;
        return new Instruction(cmd, block);
    }

    public String toString() {
        if (blockType == null) return command;
        return String.format("%s->%s", command, blockType);
    }
    
}

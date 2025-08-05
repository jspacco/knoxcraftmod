package edu.knox.knoxcraftmod.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class SerialToroProgram extends ToroProgram {
    private List<Instruction> instructions = new ArrayList<Instruction>();

    public SerialToroProgram() {}

    public SerialToroProgram(String programName, String description, List<Instruction> instructions) {
        super(programName, description);
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TYPE, SERIAL);
        tag.putString(DESCRIPTION, description);

        ListTag instructionList = toNBT(instructions);
        tag.put(INSTRUCTIONS, instructionList);

        return tag;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(programName);
        b.append("\n");
        b.append(description);
        b.append("\n");
        b.append(instructions);

        return b.toString();
    }
    
}

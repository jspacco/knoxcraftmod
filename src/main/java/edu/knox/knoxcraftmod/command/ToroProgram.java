package edu.knox.knoxcraftmod.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ToroProgram
{
    private String programName;
    private String description;
    private List<Instruction> instructions;

    public ToroProgram() {}

    public ToroProgram(String programName, String description, List<Instruction> instructions) {
        this.programName = programName;
        this.description = description;
        this.instructions = instructions;
    }

    public String getProgramName() {
        return programName;
    }

    public String getDescription() {
        return description;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("description", description);

        ListTag instructionList = new ListTag();
        for (Instruction instr : instructions) {
            instructionList.add(instr.toNBT());
        }
        tag.put("instructions", instructionList);

        return tag;
    }

    public static ToroProgram fromNBT(String name, CompoundTag tag) {
        String description = tag.getString("description");

        ListTag instructionList = tag.getList("instructions", Tag.TAG_COMPOUND);
        List<Instruction> instructions = new ArrayList<>();
        for (Tag t : instructionList) {
            if (t instanceof CompoundTag instrTag) {
                instructions.add(Instruction.fromNBT(instrTag));
            }
        }

        return new ToroProgram(name, description, instructions);
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

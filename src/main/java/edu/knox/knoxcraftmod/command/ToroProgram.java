package edu.knox.knoxcraftmod.command;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public abstract class ToroProgram
{
    protected String programName;
    protected String description;
    private static final Logger LOGGER = LogUtils.getLogger();

    public ToroProgram() {}

    public ToroProgram(String programName, String description) {
        this.programName = programName;
        this.description = description;
    }

    public String getProgramName() {
        return programName;
    }

    public String getDescription() {
        return description;
    }

    protected ListTag toNBT(List<Instruction> instructions) {
        ListTag instructionList = new ListTag();
        for (Instruction instr : instructions) {
            instructionList.add(instr.toNBT());
        }
        return instructionList;
    }

    public abstract Tag toNBT();

    public static ToroProgram fromNBT(String name, CompoundTag tag) {
        String type = tag.getString("type");
        if (!type.equals("parallel") && !type.equals("serial")){
            LOGGER.error("Unknown ToroProgram type: "+type);
            throw new IllegalStateException("Unknown ToroProgram type: "+type);
        }
        String description = tag.getString("description");

        if (type.equals("parallel")) {
            ListTag outerList = tag.getList("threads", Tag.TAG_LIST);
            List<List<Instruction>> instructions = new ArrayList<>();
            for (int i = 0; i < outerList.size(); i++) {
                ListTag innerList = (ListTag) outerList.get(i);
                List<Instruction> thread = readFromTag(innerList);
                instructions.add(thread);
            }
            return new ParallelToroProgram(name, description, instructions);
        } else {
            ListTag instructionList = tag.getList("instructions", Tag.TAG_COMPOUND);
            List<Instruction> instructions = readFromTag(instructionList);
            return new SerialToroProgram(name, description, instructions);
        }
    }
    
    private static List<Instruction> readFromTag(ListTag instructionList) {
         List<Instruction> instructions = new ArrayList<>();
        for (Tag t : instructionList) {
            if (t instanceof CompoundTag instrTag) {
                instructions.add(Instruction.fromNBT(instrTag));
            }
        }
        return instructions;
    }
}

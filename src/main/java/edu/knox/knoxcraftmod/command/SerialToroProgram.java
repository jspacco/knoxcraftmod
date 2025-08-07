package edu.knox.knoxcraftmod.command;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class SerialToroProgram extends ToroProgram {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private List<Instruction> instructions = new ArrayList<Instruction>();

    public SerialToroProgram() {}

    public SerialToroProgram(String programName, String description, List<Instruction> instructions) {
        super(programName, description);
        this.instructions = instructions;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TYPE, SERIAL);
        tag.putString(DESCRIPTION, description);
        LOGGER.trace("sanity check {}", instructions.size());
        ListTag instructionList = toNBT(instructions);
        LOGGER.debug("just created tag, length is {}", instructionList.size());
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

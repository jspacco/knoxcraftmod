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
    static final String THREADS = "threads";
    static final String SERIAL = "serial";
    static final String DESCRIPTION = "description";
    static final String PARALLEL = "parallel";
    static final String INSTRUCTIONS = "instructions";
    static final String TYPE = "type";

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

    public abstract CompoundTag toNBT();

    public static ToroProgram fromNBT(String programName, CompoundTag tag) {
        String type = tag.getString(TYPE);
        LOGGER.debug("savedata loading type {}", type);
        if (!type.equals(PARALLEL) && !type.equals(SERIAL)){
            LOGGER.error("Unknown ToroProgram type: "+type);
            throw new IllegalStateException("Unknown ToroProgram type: "+type);
        }
        String description = tag.getString(DESCRIPTION);
        LOGGER.debug("savedata loading description {}", description);

        if (type.equals(PARALLEL)) {
            ListTag outerList = tag.getList(THREADS, Tag.TAG_LIST);
            List<List<Instruction>> instructions = new ArrayList<>();
            for (int i = 0; i < outerList.size(); i++) {
                ListTag innerList = (ListTag) outerList.get(i);
                List<Instruction> thread = readFromTag(innerList);
                LOGGER.debug("parallel savedata read {} instructions", thread.size());
                instructions.add(thread);
            }
            return new ParallelToroProgram(programName, description, instructions);
        } else {
            ListTag instructionList = tag.getList(INSTRUCTIONS, Tag.TAG_COMPOUND);
            LOGGER.trace("instructionList tag {} {} {}", instructionList.getAsString(), instructionList.toString(), instructionList.size());
            List<Instruction> instructions = readFromTag(instructionList);
            LOGGER.debug("serial program just read {} instructions", instructions.size());
            return new SerialToroProgram(programName, description, instructions);
        }
    }
    
    private static List<Instruction> readFromTag(ListTag instructionList) {
        List<Instruction> instructions = new ArrayList<>();
        for (Tag t : instructionList) {
            LOGGER.debug("instruction tag type {}, instanceof CompoundTag {} and tostring {}", t.getType(), t instanceof CompoundTag, t.toString());
            if (t instanceof CompoundTag instrTag) {
                Instruction instr = Instruction.fromNBT(instrTag);
                LOGGER.trace("reading instr from savedata {}", instr);
                instructions.add(instr);
            }
        }
        return instructions;
    }
}

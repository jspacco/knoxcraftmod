package edu.knox.knoxcraftmod.command;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ParallelToroProgram extends ToroProgram {
    private List<List<Instruction>> threads;

    public ParallelToroProgram() {}

    public ParallelToroProgram(String programName, String description, List<List<Instruction>> instructions) {
        super(programName, description);
        this.threads = instructions;
    }

    public List<List<Instruction>> getThreads() {
        return this.threads;
    }

    public Tag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "parallel");
        tag.putString("description", description);
        ListTag threadTag = new ListTag();
        for (List<Instruction> thread : threads) {
            ListTag instructionList = toNBT(thread);
            threadTag.add(instructionList);
        }
        tag.put("threads", threadTag);

        return tag;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("ProgramName: "+programName);
        buf.append("\n");
buf.append("Description: "+description);
        buf.append("\n");
        buf.append(String.format("Has {} threads", threads.size()));
        buf.append("\n");

        return buf.toString();
    }

}

package edu.knox.knoxcraftmod.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.knox.knoxcraftmod.command.Instruction;
import edu.knox.knoxcraftmod.command.ToroProgram;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class ToroProgramData extends SavedData {
    public static final SavedData.Factory<ToroProgramData> FACTORY =
        new SavedData.Factory<>(
            ToroProgramData::new,       // constructor
            ToroProgramData::load,      // deserializer
            DataFixTypes.LEVEL          // type of data
        );
    
    private final Map<UUID, Map<String, ToroProgram>> programs = new HashMap<>();

    public ToroProgramData() {
        // default program
        UUID dev = UUID.fromString("380df991-f603-344c-a090-369bad2a924a");

        if (!programs.containsKey(dev)) {
            // default program for testing
            List<Instruction> instructionList = List.of(
                new Instruction("forward", "dirt"),
                new Instruction("forward", "dirt"),
                new Instruction("up", "stone"),
                new Instruction("up", "minecraft:stone"),
                new Instruction("forward", "minecraft:dirt")
            );
            ToroProgram p = new ToroProgram("test", "simple test program", instructionList);
            addProgram(dev, p);
        }
    }

    public void addProgram(UUID playerId, ToroProgram program) {
        programs.computeIfAbsent(playerId, k -> new HashMap<>()).put(program.getName(), program);
        setDirty();
    }

    public Map<String, ToroProgram> getProgramsFor(UUID playerId) {
        return programs.getOrDefault(playerId, Map.of());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        return save(tag);
    }
    
    public CompoundTag save(CompoundTag tag) {
        ListTag userList = new ListTag();

        for (var entry : programs.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, ToroProgram> userPrograms = entry.getValue();

            CompoundTag userTag = new CompoundTag();
            userTag.putUUID("uuid", uuid);

            CompoundTag programsTag = new CompoundTag();
            for (var programEntry : userPrograms.entrySet()) {
                programsTag.put(programEntry.getKey(), programEntry.getValue().toNBT());
            }

            userTag.put("programs", programsTag);
            userList.add(userTag);
        }

        tag.put("users", userList);
        return tag;
    }

    private ToroProgramData load(CompoundTag tag)
    {
        ToroProgramData data = new ToroProgramData();

        ListTag userList = tag.getList("users", Tag.TAG_COMPOUND);
        for (Tag userTagRaw : userList) {
            CompoundTag userTag = (CompoundTag) userTagRaw;
            UUID uuid = userTag.getUUID("uuid");
            CompoundTag programsTag = userTag.getCompound("programs");

            Map<String, ToroProgram> userPrograms = new HashMap<>();
            for (String key : programsTag.getAllKeys()) {
                CompoundTag programTag = programsTag.getCompound(key);
                userPrograms.put(key, ToroProgram.fromNBT(key, programTag));
            }

            data.programs.put(uuid, userPrograms);
        }
        return data;
    }

    public static ToroProgramData load(CompoundTag tag, HolderLookup.Provider provider) {
        ToroProgramData data = new ToroProgramData();
        return data.load(tag);
    }

    public static ToroProgramData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            ToroProgramData.FACTORY, 
            "toro");
    }
}

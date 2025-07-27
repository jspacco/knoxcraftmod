package edu.knox.knoxcraftmod.data;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.command.ToroProgram;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class ToroProgramData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final SavedData.Factory<ToroProgramData> FACTORY =
        new SavedData.Factory<>(
            ToroProgramData::new,       // constructor
            ToroProgramData::load,      // deserializer
            DataFixTypes.LEVEL          // type of data
        );
    
    private final Map<String, Map<String, ToroProgram>> programs = new HashMap<>();

    public void addProgram(String username, ToroProgram program) {
        programs.computeIfAbsent(username.toLowerCase(), k -> new HashMap<>()).put(program.getProgramName(), program);
        LOGGER.debug("Program {} uploaded, map is now {}", program.getProgramName(), programs);
        setDirty();
    }

    public Map<String, ToroProgram> getProgramsFor(String playerName) {
        return programs.getOrDefault(playerName.toLowerCase(), Map.of());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        return save(tag);
    }
    
    public CompoundTag save(CompoundTag tag) {
        ListTag userList = new ListTag();

        for (var entry : programs.entrySet()) {
            String username = entry.getKey();
            Map<String, ToroProgram> userPrograms = entry.getValue();

            CompoundTag userTag = new CompoundTag();
            userTag.putString("username", username);

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
            String username = userTag.getString("username");
            CompoundTag programsTag = userTag.getCompound("programs");

            Map<String, ToroProgram> userPrograms = new HashMap<>();
            for (String key : programsTag.getAllKeys()) {
                CompoundTag programTag = programsTag.getCompound(key);
                userPrograms.put(key, ToroProgram.fromNBT(key, programTag));
            }

            data.programs.put(username, userPrograms);
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

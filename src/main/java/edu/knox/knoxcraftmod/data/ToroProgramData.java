package edu.knox.knoxcraftmod.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.command.ToroProgram;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
    
    /*
    Shape of the NBT data:

{"data" : 
    {
        "dev" : 
        {
            "pyramid" : 
            {
                "type" : "serial",
                "description" : "STRING",
                "instructions" : 
                [
                    {"cmd" : "forward"},
                    {"cmd" : "setblock", "blk" : "minecraft:dirt"}
                ]
            },
            "parallelpyramid" : 
            {
                "type" : "parallel",
                "description" : "string",
                "threads" : 
                [
                    [
                        {"cmd" : "forward"},
                        {"cmd" : "right"}
                    ],
                    [
                        {"cmd" : "back"}
                        {"cmd" : "setblock", "blk" : "minecraft:dirt"}
                    ]
                ]
            }
        }
    }
}
    */
    public CompoundTag save(CompoundTag tag) {
        // iterate through Map<String, Map<String, ToroProgram>> programs
        for (Entry<String, Map<String, ToroProgram>> entry : programs.entrySet()) {
            String username = entry.getKey();
            Map<String, ToroProgram> programMap = entry.getValue();
            
            CompoundTag programTag = new CompoundTag();
            programMap.forEach((programName, toroProgram) -> {
                programTag.put(programName, toroProgram.toNBT());
            });
            tag.put(username, programTag);
        }
        return tag;
    }

    private ToroProgramData load(CompoundTag tag)
    {
        ToroProgramData data = new ToroProgramData();
        // go through each CompoundTag in tag
        for (String username : tag.getAllKeys()) {
            LOGGER.debug("ToroProgramData (SaveData) loading "+username);
            Map<String, ToroProgram> map = new HashMap<>();
            CompoundTag allProgramsTag = tag.getCompound(username);
            for (String programName : allProgramsTag.getAllKeys()) {
                LOGGER.debug("loading programName "+programName);
                CompoundTag programTag = allProgramsTag.getCompound(programName);
                ToroProgram toroProgram = ToroProgram.fromNBT(programName, programTag);
                LOGGER.debug("loaded program {} and it is {}", programName, toroProgram);
                map.put(programName, toroProgram);
            }
            data.programs.put(username, map);
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

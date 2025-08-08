package edu.knox.knoxcraftmod.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.command.TerpProgram;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class TerpProgramData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final SavedData.Factory<TerpProgramData> FACTORY =
        new SavedData.Factory<>(
            TerpProgramData::new,       // constructor
            TerpProgramData::load,      // deserializer
            DataFixTypes.LEVEL          // type of data
        );
    
    private final Map<String, Map<String, TerpProgram>> programs = new HashMap<>();

    public void addProgram(String username, TerpProgram program) {
        programs.computeIfAbsent(username.toLowerCase(), k -> new HashMap<>()).put(program.getProgramName(), program);
        LOGGER.debug("Program {} uploaded, map is now {}", program.getProgramName(), programs);
        setDirty();
    }

    public Map<String, TerpProgram> getProgramsFor(String playerName) {
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
        // iterate through Map<String, Map<String, TerpProgram>> programs
        for (Entry<String, Map<String, TerpProgram>> entry : programs.entrySet()) {
            String username = entry.getKey();
            Map<String, TerpProgram> programMap = entry.getValue();
            
            CompoundTag programTag = new CompoundTag();
            programMap.forEach((programName, terpProgram) -> {
                programTag.put(programName, terpProgram.toNBT());
            });
            tag.put(username, programTag);
        }
        return tag;
    }

    private TerpProgramData load(CompoundTag tag)
    {
        TerpProgramData data = new TerpProgramData();
        // go through each CompoundTag in tag
        for (String username : tag.getAllKeys()) {
            LOGGER.debug("TerpProgramData (SaveData) loading "+username);
            Map<String, TerpProgram> map = new HashMap<>();
            CompoundTag allProgramsTag = tag.getCompound(username);
            for (String programName : allProgramsTag.getAllKeys()) {
                LOGGER.debug("loading programName "+programName);
                CompoundTag programTag = allProgramsTag.getCompound(programName);
                TerpProgram terpProgram = TerpProgram.fromNBT(programName, programTag);
                LOGGER.debug("loaded program {} and it is {}", programName, terpProgram);
                map.put(programName, terpProgram);
            }
            data.programs.put(username, map);
        }
        return data;
    }
    
    public static TerpProgramData load(CompoundTag tag, HolderLookup.Provider provider) {
        TerpProgramData data = new TerpProgramData();
        return data.load(tag);
    }

    public static TerpProgramData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            TerpProgramData.FACTORY, 
            "terp");
    }
}

package edu.knox.knoxcraftmod.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;

import edu.knox.knoxcraftmod.command.TerpProgram;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class TerpProgramData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();


    /* -------------------------------------------------------------
     * Passthrough CODEC: delegates to our existing NBT format
     * ------------------------------------------------------------- */
    public static final Codec<TerpProgramData> CODEC =
        Codec.PASSTHROUGH.comapFlatMap(
            // decode: Dynamic<?> -> TerpProgramData
            dyn -> {
                try {
                    CompoundTag root = (CompoundTag) dyn.convert(NbtOps.INSTANCE).getValue();
                    TerpProgramData data = new TerpProgramData();
                    data.readFromRootTag(root);
                    return DataResult.success(data);
                } catch (Exception e) {
                    return DataResult.error(() -> "TerpProgramData decode failed: " + e.getMessage());
                }
            },
            // encode: TerpProgramData -> Dynamic<?>
            data -> {
                CompoundTag root = data.writeToRootTag();
                return new Dynamic<>(NbtOps.INSTANCE, root);
            }
        );

     public static final SavedDataType<TerpProgramData> TYPE_WITH_CTX =
        new SavedDataType<>(
            "terp",
            ctx -> new TerpProgramData(),
            ctx -> CODEC,
            DataFixTypes.LEVEL
        );
    
    /* -------------------------------------------------------------
     * Codec helpers: keep your existing NBT layout intact
     * ------------------------------------------------------------- */
    private void readFromRootTag(CompoundTag root) {
        for (String username : root.keySet()) {
            CompoundTag programsByUser = root.getCompound(username).get();
            Map<String, TerpProgram> byName = new HashMap<>();
            for (String programName : programsByUser.keySet()) {
                CompoundTag programTag = programsByUser.getCompound(programName).get();
                TerpProgram tp = TerpProgram.fromNBT(programName, programTag);
                byName.put(programName, tp);
            }
            programs.put(username, byName);
        }
    }

    private CompoundTag writeToRootTag() {
        CompoundTag root = new CompoundTag();
        for (var entry : programs.entrySet()) {
            String username = entry.getKey();
            CompoundTag byUser = new CompoundTag();
            for (var p : entry.getValue().entrySet()) {
                String programName = p.getKey();
                TerpProgram tp = p.getValue();
                byUser.put(programName, tp.toNBT());
            }
            root.put(username, byUser);
        }
        return root;
    }

    public static final SavedDataType<TerpProgramData> TYPE = 
        new SavedDataType<>("terp", TerpProgramData::new, CODEC, DataFixTypes.LEVEL);
            
    
    private final Map<String, Map<String, TerpProgram>> programs = new HashMap<>();

    public void addProgram(String username, TerpProgram program) {
        programs.computeIfAbsent(username.toLowerCase(), k -> new HashMap<>()).put(program.getProgramName(), program);
        LOGGER.debug("Program {} uploaded, map is now {}", program.getProgramName(), programs);
        setDirty();
    }

    public Map<String, TerpProgram> getProgramsFor(String playerName) {
        return programs.getOrDefault(playerName.toLowerCase(), Map.of());
    }

    // @Override
    // public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
    //     return save(tag);
    // }
    
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

    public TerpProgramData load(CompoundTag tag)
    {
        TerpProgramData data = new TerpProgramData();
        // go through each CompoundTag in tag
        for (String username : tag.keySet()) {
            LOGGER.debug("TerpProgramData (SaveData) loading "+username);
            Map<String, TerpProgram> map = new HashMap<>();
            CompoundTag allProgramsTag = tag.getCompound(username).get();
            for (String programName : allProgramsTag.keySet()) {
                LOGGER.debug("loading programName "+programName);
                CompoundTag programTag = allProgramsTag.getCompound(programName).get();
                TerpProgram terpProgram = TerpProgram.fromNBT(programName, programTag);
                LOGGER.debug("loaded program {} and it is {}", programName, terpProgram);
                map.put(programName, terpProgram);
            }
            data.programs.put(username, map);
        }
        return data;
    }

    public TerpProgramData create() {
        return new TerpProgramData();
    }

    public static TerpProgramData load(CompoundTag tag, HolderLookup.Provider provider) {
        TerpProgramData data = new TerpProgramData();
        return data.load(tag);
    }

    // public static TerpProgramData get(ServerLevel level) {
    //     return level.getDataStorage().computeIfAbsent(
    //         TerpProgramData.FACTORY, 
    //         "terp");
    // }

    public static TerpProgramData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE_WITH_CTX);
    }
}

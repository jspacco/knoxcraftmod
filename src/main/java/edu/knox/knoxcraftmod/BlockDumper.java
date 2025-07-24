package edu.knox.knoxcraftmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;

public class BlockDumper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void dumpAllBlocks(ServerLevel level) {
        Registry<Block> registry = level.registryAccess().registryOrThrow(Registries.BLOCK);

        StringBuffer buf = new StringBuffer("\n\n");
        buf.append("public enum BlockType {\n");

        for (ResourceLocation id : registry.keySet()) {
            String name = id.getPath().toUpperCase().replaceAll("[^A-Z0-9_]", "_");
            String line = String.format("    %s(\"%s\"),", name, id.toString());
            buf.append(line);
            buf.append("\n");
        }

        buf.append("    ;\n\n    private final String id;\n\n    BlockType(String id) {\n        this.id = id;\n    }\n\n    public String getId() {\n        return id;\n    }\n}\n");
        LOGGER.debug(buf.toString());
    }
    
}


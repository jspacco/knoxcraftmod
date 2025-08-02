package tools;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;

public class BlockDumper {
    private static final Logger LOGGER = LogUtils.getLogger();

    // SKIP:
    // door, inventory, button
    // pressure_plate
    // trapdoor
    // stage
    // bell_
    // dripleaf
    // composter
    // command block
    // 

    // https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.21.8/assets/minecraft/textures/block/dirt.png
    private static final String TEXTURE_BASE_URL = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.21.5/assets/minecraft/textures/block/";
    private static final String OUTPUT_FILE = "config/knoxcraftmod/block_dump.txt";

    public static void dumpBlockModels(ServerLevel level) throws IOException {
        //MinecraftServer server = level.getServer();
        ResourceManager manager = Minecraft.getInstance().getResourceManager();

        List<ResourceLocation> blockModelIds = manager.listResources("models/block", rl -> rl.getPath().endsWith(".json"))
            .keySet().stream().sorted().collect(Collectors.toList());

        List<String> outputLines = new ArrayList<>();
        outputLines.add("package edu.knox.knoxcraftmod.client;\n");
        outputLines.add("public enum ToroBlockType {\n");
        outputLines.add("\n");

        for (ResourceLocation id : blockModelIds) {
            LOGGER.debug(id.toString());
            Optional<Resource> resourceOpt = manager.getResource(id);

            if (resourceOpt.isEmpty()) continue;

            try (InputStream in = resourceOpt.get().open()) {
                String blockName = id.getPath().replace("models/block/", "").replace(".json", "").replace("minecraft:", "");
                LOGGER.debug("blockName = "+blockName);
                JsonObject model = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
                JsonObject textures = model.has("textures") ? model.getAsJsonObject("textures") : null;
                if (textures == null || textures.size() == 0) continue;

                boolean isComposite = textures.size() > 1;
                StringBuilder doc = new StringBuilder();
                doc.append("    /**\n");
                if (isComposite) {
                    doc.append("     * Composite block\n");
                    doc.append("     * <ul>\n");
                    for (Map.Entry<String, com.google.gson.JsonElement> texture : textures.entrySet()) {
                        String label = capitalize(texture.getKey());
                        String texturePath = texture.getValue().getAsString().replace("minecraft:block/", "");
                        String url = TEXTURE_BASE_URL + texturePath + ".png";
                        doc.append(String.format("     * %s: <img src=\"%s\" width=128 height=128 alt=\"%s\" />\n", label, url, blockName));
                        //doc.append("     *   <li>").append(label).append(": <img src=\"").append(url).append("\"/></li>\n");
                    }
                    doc.append("     * </ul>\n");
                } else {
                    Map.Entry<String, com.google.gson.JsonElement> texture = textures.entrySet().iterator().next();
                    String label = capitalize(texture.getKey());
                    String texturePath = texture.getValue().getAsString().replace("minecraft:block/", "");
                    String url = TEXTURE_BASE_URL + texturePath + ".png";
                    doc.append(String.format("     * %s: <img src=\"%s\" width=128 height=128 alt=\"%s\" />\n", label, url, blockName));
                    //doc.append("     * ").append(label).append(": <img src=\"").append(url).append("\"/ width=128 height=128 >\n");
                }
                doc.append("     */\n");

                String enumName = blockName.toUpperCase().replaceAll("[^A-Z0-9]", "_");
                String enumLine = "    " + enumName + "(\"minecraft:" + blockName + "\"),";

                outputLines.add(doc.toString());
                outputLines.add(enumLine);
            }
        }

        outputLines.add("""
                ;

    private final String id;

    ToroBlockType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
""");


        Path outputPath = Paths.get(OUTPUT_FILE);
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, outputLines);
        
    }

    private static String capitalize(String s) {
        return s.length() > 0 ? s.substring(0, 1).toUpperCase() + s.substring(1) : s;
    }
}

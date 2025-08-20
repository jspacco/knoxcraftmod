package tools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.util.Msg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DumpBlocksCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static Path outDir = FMLPaths.GAMEDIR.get().resolve("dumps");
    private static Path outFile = outDir.resolve("blocks-1.21.8.txt");
    
    private DumpBlocksCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            LiteralArgumentBuilder.<CommandSourceStack>literal("dumpblocks2")
                .requires(src -> src.hasPermission(4))
                .executes(ctx -> execute(ctx.getSource()))
        );
    }

    private static int execute(CommandSourceStack src) {
        try {
            Files.createDirectories(outDir);
            List<String> lines = new ArrayList<>(List.of(
                "package edu.knox.knoxcraftmod.client;",
                "public enum TerpBlockType {",
                ""));

            lines.addAll(BuiltInRegistries.BLOCK.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().location().toString()))
                .map(entry -> formatBlockEntry(entry.getKey().location(), entry.getValue()))
                .collect(Collectors.toList()));
            
            lines.add("""
                ;

    private final String id;

    TerpBlockType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
""");

            Files.write(outFile, lines, StandardCharsets.UTF_8);
            Msg.reply(src, 
                "Dumped " + (lines.size() - 4) + " blocks to " + outFile.toAbsolutePath(), 
                false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            LOGGER.error("exception", e);
            Msg.fail(src, "Failed");
            return 0;
        }
    }

    private static String formatBlockEntry(ResourceLocation id, Block block) {
        //BlockState state = block.defaultBlockState();
        String blockName = id.getPath().replace("models/block/", "").replace(".json", "").replace("minecraft:", "");

        String enumName = blockName.toUpperCase().replaceAll("[^A-Z0-9]", "_");
        String enumLine = "    " + enumName + "(\"minecraft:" + blockName + "\"),";

        LOGGER.trace(enumLine.strip());

        return enumLine;
    }
}


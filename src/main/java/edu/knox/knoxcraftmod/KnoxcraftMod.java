package edu.knox.knoxcraftmod;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.command.TerpCommand;
import edu.knox.knoxcraftmod.http.HttpServerManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(KnoxcraftMod.MODID)
public class KnoxcraftMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "knoxcraftmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public KnoxcraftMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // register KnoxcraftMod specific config information
        // putting into COMMON rather than SERVERCONFIG so that
        // it's in one easy to find place (config) and not 
        // in a per-world location (saves/WORLD_NAME/serverconfig)
        // that needs to be set for every new world
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, KnoxcraftConfig.COMMON_CONFIG);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // start HTTP server
        try {
            HttpServerManager.start(event.getServer());
        } catch (Exception e) {
            LOGGER.error("Failed to start HTTP server", e);
        }

        // create server.properties if it doesn't exist
        // ensure flat world and no structures
        Path propsPath = Paths.get("server.properties");
        if (!Files.exists(propsPath)) {
            try {
                List<String> defaultProps = List.of(
                    "level-type=flat",
                    "generate-structures=false",
                    "gamemode=creative",
                    "spawn-monsters=false",
                    "# must be true or the server throws out spawns",
                    "spawn-animals=true", // this has to be set to true
                    "motd=Knoxcraft Superflat Server",
                    "spawn-npcs=false",
                    "difficulty=easy"
                );
                Files.write(propsPath, defaultProps);
                KnoxcraftMod.LOGGER.info("Generated default server.properties");
            } catch (IOException e) {
                KnoxcraftMod.LOGGER.error("Failed to write server.properties", e);
            }
        }
    }

    @SubscribeEvent
    public static <FMLServerStoppingEvent> void onServerStopping(FMLServerStoppingEvent event) {
        HttpServerManager.stop();
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    // regsiter for slash commands
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TerpCommand.register(event.getDispatcher());
    }

}

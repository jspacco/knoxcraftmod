package edu.knox.knoxcraftmod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KnoxcraftConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final ForgeConfigSpec.IntValue httpPort;
    private static final ForgeConfigSpec.BooleanValue loginRequired;

    public static int HTTP_PORT;
    public static boolean LOGIN_REQUIRED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("httpserver");
        httpPort = builder
            .comment("The port the HTTP server listens on")
            .defineInRange("port", 8080, 1024, 65535);

        loginRequired = builder
            .comment("Whether login is required for the web interface")
            .define("loginRequired", false);
        builder.pop();

        COMMON_CONFIG = builder.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        HTTP_PORT = httpPort.get();
        LOGIN_REQUIRED = loginRequired.get();
    }
}


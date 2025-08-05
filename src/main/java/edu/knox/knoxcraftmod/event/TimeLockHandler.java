package edu.knox.knoxcraftmod.event;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import edu.knox.knoxcraftmod.KnoxcraftMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID)
public class TimeLockHandler {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        // Only run on the server, only after the tick
        if (event.level instanceof ServerLevel serverLevel
                && event.phase == TickEvent.Phase.END
                && serverLevel.dimension() == Level.OVERWORLD) {

            // Keep it noon forever
            serverLevel.setDayTime(6000);
            // It's always sunny in Minecraftlandia
            serverLevel.setWeatherParameters(0, 0, false, false);
        }
    }
}


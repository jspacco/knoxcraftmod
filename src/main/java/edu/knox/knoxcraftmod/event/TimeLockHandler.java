package edu.knox.knoxcraftmod.event;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import edu.knox.knoxcraftmod.KnoxcraftMod;
import net.minecraft.server.level.ServerLevel;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID)
public class TimeLockHandler {

    @SubscribeEvent
    public static void onLevelTickPost(TickEvent.LevelTickEvent.Post event) {
        if (event.side.isServer() && 
            event.level instanceof ServerLevel level)
        {
            // eternal noon
            level.setDayTime(6000);
            // it's always sunny in Minecraftlandia
            level.setWeatherParameters(0, 0, false, false);
        }
    }
}


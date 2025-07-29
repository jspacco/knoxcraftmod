package edu.knox.knoxcraftmod.event;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.level.BlockEvent.PortalSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PortalBlocker {

    @SubscribeEvent
    public void onEntityTeleport(EntityTravelToDimensionEvent event) {
        // prevent entities from teleporting
        if (event.getDimension() == Level.NETHER || event.getDimension() == Level.END) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("Portal travel is disabled."));
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPortalSpawn(PortalSpawnEvent event) {
        // also prevent spawning of portals, just to be sure
        event.setCanceled(true);
    }

}

package edu.knox.knoxcraftmod.event;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnHandler {
    private static final Logger LOGGER = LogUtils.getLogger(); 
    
    @SubscribeEvent
    public void onLeave(EntityLeaveLevelEvent e) {
        if (e.getLevel().isClientSide()) return;
        var ent = e.getEntity();

        // MC has an internal removal reason; if available in your version, print it.
        // (getRemovalReason() returns null if still present or not set)
        String reason = null;
        try {
            var rr = ent.getRemovalReason(); // may exist in your 1.21.5 mappings
            reason = (rr == null) ? "unknown" : rr.toString();
        } catch (Throwable ignored) {
            reason = "unknown";
        }

        LOGGER.debug("[LEAVE] " + ent.getType().toShortString() + " id=" + ent.getId() +
            " reason=" + reason + " pos=" + ent.position());
    }

    
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {

        // Only enforce on the server; vanilla clients never run this mod.
        if (event.getLevel().isClientSide()) return;

        var e = event.getEntity();

        // Let players through
        if (e instanceof Player) return;

        // Allow turtles so vanilla clients can see "Terps" 
        // (which spawn as a vanilla turtle on client).
        if (e.getType() == net.minecraft.world.entity.EntityType.TURTLE) {
            LOGGER.debug("allowing spawn of turtle {}", e);
            return;
        }

        LOGGER.debug("Canceling spawn of {}", e.getClass());

        // Block other mobs.
        event.setCanceled(true);
    }
}


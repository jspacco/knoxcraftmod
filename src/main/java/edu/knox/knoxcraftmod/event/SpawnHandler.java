package edu.knox.knoxcraftmod.event;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnHandler {
    private static final Logger LOGGER = LogUtils.getLogger(); 
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Only enforce on the server; vanilla clients never run this mod.
        if (event.getLevel().isClientSide()) return;

        // If you disabled mob spawning in server.properties, bail out early.
        if (!event.getLevel().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;

        var e = event.getEntity();

        // Let players through
        if (e instanceof Player) return;

        // Allow turtles so vanilla clients can see your "Terp" (which you'll spawn as a vanilla turtle).
        if (e.getType() == net.minecraft.world.entity.EntityType.TURTLE) return;

        LOGGER.debug("Canceling spawn of {}", e.getClass());

        // Block other mobs.
        event.setCanceled(true);
    }
}


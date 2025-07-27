package edu.knox.knoxcraftmod.event;

import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//@Mod.EventBusSubscriber(modid = "knoxcraftmod")
@Mod.EventBusSubscriber(modid = "knoxcraftmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Allow player-spawned entities and your custom Toro
        if (event.getEntity() instanceof Player || event.getEntity() instanceof TorosaurusEntity) {
            return;
        }

        // Cancel everything else
        event.setCanceled(true);
    }
}


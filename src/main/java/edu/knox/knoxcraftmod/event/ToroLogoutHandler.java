package edu.knox.knoxcraftmod.event;

import edu.knox.knoxcraftmod.entity.ModEntities;
import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mod.EventBusSubscriber(modid = "knoxcraftmod")
public class ToroLogoutHandler {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Despawn all Toros that belong to this player
        player.serverLevel().getEntities(ModEntities.TOROSAURUS.get(), 
            toro -> toro instanceof TorosaurusEntity torosaurus &&
                    torosaurus.getOwnerUUID().equals(player.getUUID()))
            .forEach(Entity::discard);
    }
}

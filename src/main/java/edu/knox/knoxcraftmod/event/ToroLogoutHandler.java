package edu.knox.knoxcraftmod.event;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.command.TurtleCommand;
import edu.knox.knoxcraftmod.entity.ModEntities;
import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mod.EventBusSubscriber(modid = "knoxcraftmod")
public class ToroLogoutHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Despawn all Toros that belong to this player
        player.serverLevel().getEntities(ModEntities.TOROSAURUS.get(), 
            toro -> toro instanceof TorosaurusEntity torosaurus &&
                    torosaurus.getOwnerUUID().equals(player.getUUID()))
            .forEach(toro -> {
                String username = event.getEntity().getGameProfile().getName();
                LOGGER.debug("removing Toro for "+ username);
                TurtleCommand.removeToro(player.getUUID());
                toro.discard();
            });
    }
}

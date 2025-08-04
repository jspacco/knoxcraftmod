package edu.knox.knoxcraftmod.event;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.command.ToroCommand;
import edu.knox.knoxcraftmod.entity.ModEntities;
import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID)
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
                toro.stop();
                String username = event.getEntity().getGameProfile().getName();
                LOGGER.info("removing Toro for "+ username+" on logout");
                ToroCommand.removeToroMapping(player.getUUID());
            });
    }
}

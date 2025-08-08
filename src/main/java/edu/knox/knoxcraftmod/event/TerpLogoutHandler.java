package edu.knox.knoxcraftmod.event;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.command.TerpCommand;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID)
public class TerpLogoutHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        LOGGER.info("logging out {}", player.getGameProfile().getName());
        TerpCommand.logout(player);
    }
}

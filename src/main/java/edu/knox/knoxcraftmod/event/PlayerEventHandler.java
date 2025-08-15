// PlayerEventHandler.java
package edu.knox.knoxcraftmod.event;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.util.Msg;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * EventHandler will cancel/prevent these actions:
 *  - manually placing blocks
 *  - manually breaking blocks
 *  - using items
 *  - picking up items
 */
@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            event.setCanceled(true);
            Msg.send(player, "Manually placing blocks is disabled! Write code to make your Terp do it for you", true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player instanceof ServerPlayer sPlayer) {
            if (player != null && sPlayer.createCommandSourceStack().hasPermission(4)){
                // ops can break blocks
                return;
            }
        }
        if (event.getPlayer() != null) {
            event.setCanceled(true);
            Msg.send(event.getPlayer(), 
                "Manually breaking blocks is disabled! Write code to make your Terp do it for you",
                true);
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof Player player) {
            event.setCanceled(true);
            Msg.send(player, 
                "Item use is disabled.",
                true);
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof Player player &&
            player.isCreative()) {
            event.setCanceled(true);
            Msg.send(player, "Item pickup is disabled.", true);
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        // Cancel dropping items
        event.setCanceled(true);

        Msg.send(event.getPlayer(), "Dropping items is disabled.", true);
    }

}

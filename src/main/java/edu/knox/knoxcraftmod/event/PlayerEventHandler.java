// PlayerEventHandler.java
package edu.knox.knoxcraftmod.event;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.util.Msg;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
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

    @SubscribeEvent(alwaysCancelling=true)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            Msg.send(player, "Manually placing blocks is disabled! Write code to make your Terp do it for you", true);
        }
    }

    @SubscribeEvent
    public static boolean onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return true;
        if (player instanceof ServerPlayer sPlayer
            && sPlayer.getPermissionLevel() == 4)
        {
            // ops can break blocks
            return false;
        }
        // send message to player and cancel for everyone else
        Msg.send(event.getPlayer(), 
            "Manually breaking blocks is disabled! Write code to make your Terp do it for you",
            true);
        return true;
    }

    @SubscribeEvent(alwaysCancelling=true)
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        //TODO: allow for operators
        Player player = event.getEntity();
        if (player == null) return;
        Msg.send(player, 
            "Item use is disabled.",
            true);
    }

    @SubscribeEvent
    public static boolean onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        if (player.hasPermissions(4)) {
            // allow for operators
            return false;
        }
        // send message to players
        // cancel for everyone else
        Msg.send(player, "Item pickup is disabled.", true);
        return true;
    }

    @SubscribeEvent(alwaysCancelling=true)
    public static void onItemToss(ItemTossEvent event) {
        // Cancel dropping items
        Msg.send(event.getPlayer(), "Dropping items is disabled.", true);
    }

}

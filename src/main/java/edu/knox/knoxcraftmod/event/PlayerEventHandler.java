// PlayerEventHandler.java
package edu.knox.knoxcraftmod.event;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.util.Msg;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
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
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent(alwaysCancelling=true)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            Msg.send(player, "Manually placing blocks is disabled! Write code to make your Terp do it for you", true);
        }
    }

    @SubscribeEvent
    public static boolean onBlockBreak(BlockEvent.BreakEvent event) {
        
        Player player = event.getPlayer();
        if (player == null) {
            LOGGER.debug("player for break block event is null");
            return false;
        }
        LOGGER.debug("player {} with permission level {}", player.getName(), player.getPermissionLevel());
        if (player.getPermissionLevel() == 4)
        {
            // ops can break blocks
            LOGGER.debug("player {} with permission level {} allowed to break blocks", player.getName(), player.getPermissionLevel());
            return false;
        }
        // send message to player and cancel for everyone else
        Msg.send(event.getPlayer(), 
            "Manually breaking blocks is disabled! Write code to make your Terp do it for you",
            true);
        boolean returnVal = true;
        LOGGER.debug("break block event returning: "+returnVal);
        return returnVal;
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

    @SubscribeEvent
    public static boolean onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
        Player p = e.getEntity();
        if (p != null && p.hasPermissions(4)) return false;
        Msg.send(p, "Trying to cancel block breaking.", true);
        return true; // cancel
    }

}

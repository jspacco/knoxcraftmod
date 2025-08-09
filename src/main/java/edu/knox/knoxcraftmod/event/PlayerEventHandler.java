// PlayerEventHandler.java
package edu.knox.knoxcraftmod.event;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraft.network.chat.Component;
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
            player.sendSystemMessage(Component.literal("Manually placing blocks is disabled! Write code to make your Toro do it for you"));
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null) {
            event.setCanceled(true);
            event.getPlayer().sendSystemMessage(Component.literal("Manually breaking blocks is disabled! Write code to make your Toro do it for you"));
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().isCreative()) {
            event.setCanceled(true);
            event.getEntity().sendSystemMessage(Component.literal("Item use is disabled."));
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof Player player &&
            player.isCreative()) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("Item pickup is disabled."));
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        // Cancel drop entirely
        event.setCanceled(true);

        // Optional: tell the player why
        event.getPlayer().displayClientMessage(
            Component.literal("Dropping items is disabled."),
            true // action bar instead of chat
        );
    }

}

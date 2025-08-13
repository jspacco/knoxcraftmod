package edu.knox.knoxcraftmod.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.CommandSourceStack;

/**
 * Shim file for messaging, sicne this tends to change
 * between releases of MC and Forge
 */
public final class Msg {
    private Msg() {}

    /** Send to a player; actionBar = true => overlay, false => chat */
    public static void send(Player player, Component msg, boolean actionBar) {
        if (player instanceof ServerPlayer sp) {
            // Server side path (1.21 requires the boolean)
            sp.sendSystemMessage(msg, actionBar);
        } else {
            // Client logical side (e.g., LocalPlayer in singleplayer)
            player.displayClientMessage(msg, actionBar);
        }
    }

     /** 
      * Send to a player; actionBar = true => overlay, false => chat. 
      * Converts msg to Components.
      */
    public static void send(Player player, String msg, boolean actionBar) {
        send(player, text(msg), actionBar);
    }

    /** Convenience: default to normal chat (not action bar). */
    public static void send(Player player, Component msg) {
        send(player, msg, false);
    }

    /** Syntactic sugar */
    public static void send(Player player, String msg) {
        send(player, text(msg), false);
    }

    /**
     * Sends a failure messge. I think this will
     * be a stable API, but who knows?
     * 
     * @param src
     * @param text
     */
    public static void fail(CommandSourceStack src, String text) {
        src.sendFailure(Component.literal(text));
    }


    /** Broadcast to all online players. */
    public static void broadcast(MinecraftServer server, Component msg, boolean actionBar) {
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            send(sp, msg, actionBar);
        }
    }

    /**
     * Syntactic sugar method
     * @param src
     * @param msg message to be converted to 
     * @param broadcastToOps
     */
    public static void reply(CommandSourceStack src, String msg, boolean broadcastToOps) {
        reply(src, text(msg), broadcastToOps);
    }

    /**
     * Reply from a command
     * @param src
     * @param msg
     * @param broadcastToOps broadcastToOps = false mirrors vanilla feedback
     */
    public static void reply(CommandSourceStack src, Component msg, boolean broadcastToOps) {
        // CommandSourceStack API in 1.21: sendSuccess(Supplier<Component>, boolean)
        src.sendSuccess(() -> msg, broadcastToOps);
    }

    /** Overload without ops broadcast. */
    public static void reply(CommandSourceStack src, Component msg) {
        reply(src, msg, false);
    }

    /** Sugar for literal text; prefer translatable keys in real code. */
    public static Component text(String s) {
        return Component.literal(s);
    }

    /** Example translatable usage */
    public static Component tr(String key, Object... args) {
        return Component.translatable(key, args);
    }
}


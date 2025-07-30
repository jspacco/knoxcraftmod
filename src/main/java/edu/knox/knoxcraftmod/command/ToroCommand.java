package edu.knox.knoxcraftmod.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import edu.knox.knoxcraftmod.entity.ModEntities;
import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import edu.knox.knoxcraftmod.data.ToroProgramData;
import net.minecraft.network.chat.Component;

public class ToroCommand 
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Map<UUID, TorosaurusEntity> toroMap = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("toro")
                .then(Commands.literal("summon")
                    .executes(ctx -> summonToro(ctx.getSource())))
                .then(Commands.literal("run")
                    .then(Commands.argument("program", StringArgumentType.string())
                        .executes(ctx -> runProgram(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "program")))))
                .then(Commands.literal("list")
                    .executes(ctx -> listPrograms(ctx.getSource())))
                .then(Commands.literal("stop")
                    .executes(ctx -> stopToro(ctx.getSource())))

        );
        // manual movement commands
        LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal("toro");

        for (String action : List.of("forward", "back",
             "up", "down", "left", "right", 
             "turnleft", "tl", "turnright", "tr")) 
        {
            base.then(Commands.literal(action)
                .executes(ctx -> manualMove(ctx, action)));
        }
        dispatcher.register(base);
    }

    private static int manualMove(CommandContext<CommandSourceStack> ctx, String action) {
        ServerPlayer player = ctx.getSource().getPlayer();

        TorosaurusEntity toro = getToro(player.getUUID());
        if (toro == null) {
            // Toro must already exist for a manual move
            ctx.getSource().sendFailure(Component.literal("Toro not found."));
            return 0;
        }

        if (toro.isRunning()) {
            // Toro must already exist for a manual move
            ctx.getSource().sendFailure(Component.literal("Toro is busy! Use '/toro stop' to stop the Toro first. "));
            return 0;
        }

        toro.moveToro(action);

        ctx.getSource().sendSuccess(() -> Component.literal("Toro moved: " + action), false);
        return 1;
    }

    private static int summonToro(CommandSourceStack source)
    {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        if (player.getY() >= level.getMaxBuildHeight()) {
            source.sendFailure(Component.literal("Cannot summon Toro above max build height. "));
            return 0;
        }
        TorosaurusEntity toro = getOrCreateToro(player, level);
        if (toro.isRunning()) {
            source.sendFailure(Component.literal("Toro is busy! Use '/toro stop' to stop the Toro. "));
            return 0;
        }
        moveToroToPlayer(toro, player);
        source.sendSuccess(() -> Component.literal("Toro summoned."), false);
        return 1;
    }

    private static int stopToro(CommandSourceStack source)
    {
        ServerPlayer player = source.getPlayer();
        TorosaurusEntity toro = getToro(player.getUUID());
        if (toro == null) {
            source.sendFailure(Component.literal("No Toro to stop. "));
            return 0;
        }
        toro.stop();
        source.sendSuccess(() -> Component.literal("Toro stopped."), false);
        return 1;
    }

    private static TorosaurusEntity getOrCreateToro(ServerPlayer player, ServerLevel level)
    {
        // get the player's toro, if one exists
        TorosaurusEntity toro = getToro(player.getUUID());
        if (toro != null) return toro;

        // can't find the toro, so make a new one
        toro = new TorosaurusEntity(ModEntities.TOROSAURUS.get(), level);
        LOGGER.debug("Spawning new Toro with uuid {}", toro.getUUID());
        setToro(player.getUUID(), toro);
        toro.setOwnerUUID(player.getUUID());
        // It feels like I should call moveToroToPlayer(toro, player) here, 
        // but I know it will get called anytime I summon a toro, so I would be doing
        // that twice. But it still feels like I should set the location here.

        // add Toro to the level
        level.addFreshEntity(toro);
        
        return toro;
    }

    private static void moveToroToPlayer(TorosaurusEntity toro, ServerPlayer player)
    {
        toro.setPos(player.getX(), player.getY(), player.getZ());
        Direction dir = Direction.fromDegrees(player.getYRot());
        toro.setToroDirection(dir);
    }

    private static void setToro(UUID uuid, TorosaurusEntity toro) {
        toroMap.put(uuid, toro);
    }

    public static TorosaurusEntity getToro(UUID uuid) {
        return toroMap.get(uuid);
    }

    private static int runProgram(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.serverLevel();

        TorosaurusEntity toro = getToro(player.getUUID());
        
        if (toro == null) {
            source.sendFailure(Component.literal("First summon your Toro with '/toro summon'"));
            return 0;
        }

        if (toro.isRunning()) {
            source.sendFailure(Component.literal("Toro is busy! Wait or stop '/toro stop' "));
            return 0;
        }

        // this should always return something,
        // becuase if it's not there it makes a new one
        ToroProgramData data = ToroProgramData.get(level);

        String playerName = player.getGameProfile().getName();
        LOGGER.debug("Game Profile name is "+playerName);
        ToroProgram program = data.getProgramsFor(player.getGameProfile().getName()).get(name);
        if (program == null) {
            source.sendFailure(Component.literal(String.format("Program '%s' not found.", name)));
            return 0;
        }

        toro.runProgram(program);
        source.sendSuccess(() -> Component.literal("Program loaded!"), false);
        return 1;
    }

    private static int listPrograms(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.serverLevel();
        ToroProgramData data = ToroProgramData.get(level);

        String playerName = player.getGameProfile().getName();
        LOGGER.debug("Game Profile playerName is "+playerName);
        var map = data.getProgramsFor(playerName);
        if (map.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No programs found."), false);
            return 0;
        }

        for (String name : map.keySet()) {
            LOGGER.debug("Program name: "+name);
            source.sendSuccess(() -> Component.literal("-> " + name), false);
        }

        return 1;
    }

    public static void removeToro(UUID uuid) {
        if (uuid != null){
            toroMap.remove(uuid);
        }
    }

}

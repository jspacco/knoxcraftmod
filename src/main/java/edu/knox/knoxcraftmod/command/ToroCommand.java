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

        toro.moveToro(action);

        ctx.getSource().sendSuccess(() -> Component.literal("Toro moved: " + action), false);
        return 1;
    }

    private static int summonToro(CommandSourceStack source)
    {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        getOrSpawnToro(player, level);
        source.sendFailure(Component.literal("Toro summoned."));
        return 1;
    }

    private static TorosaurusEntity getOrSpawnToro(ServerPlayer player, ServerLevel level)
    {
        // get the player's toro, if one exists
        TorosaurusEntity toro = getToro(player.getUUID());
        if (toro == null) {
            // can't find the toro, so make a new one
            toro = new TorosaurusEntity(ModEntities.TOROSAURUS.get(), level);
        }
        LOGGER.debug("Summoning Toro with uuid {}", toro.getUUID());
        toro.setPos(player.getX(), player.getY(), player.getZ());
        toro.setOwnerUUID(player.getUUID()); 
        
        // TODO: figure out how to get the direction correct
        //toro.setYRot(player.getYRot());
        //toro.setRot(player.getYRot(), player.getXRot());
        Direction dir = Direction.fromDegrees(player.getYRot());
        toro.setToroDirection(dir);
        //toro.updateDirectionFromRotation();
        level.addFreshEntity(toro);
        // save the toro in the manager
        setToro(player.getUUID(), toro);
        return toro;
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

        // Find the player's Toro
        // or create a new one if they don't already have one
        TorosaurusEntity toro = getOrSpawnToro(player, level);

        if (toro == null) {
            LOGGER.error("Cannot find or create a Toro");
            source.sendFailure(Component.literal("Cannot find or create a Toro. Major error"));
            return 0;
        }

        // this should always return something,
        // becuase if it's not there it makes a new one
        ToroProgramData data = ToroProgramData.get(level);

        String playerName = player.getGameProfile().getName();
        LOGGER.debug("Game Profile name is "+playerName);
        ToroProgram program = data.getProgramsFor(player.getGameProfile().getName()).get(name);
        if (program == null) {
            source.sendFailure(Component.literal("Program not found."));
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

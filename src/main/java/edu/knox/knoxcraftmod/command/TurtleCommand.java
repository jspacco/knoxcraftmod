package edu.knox.knoxcraftmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import edu.knox.knoxcraftmod.entity.ModEntities;
import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import edu.knox.knoxcraftmod.data.ToroProgramData;
import net.minecraft.nbt.CompoundTag;

public class TurtleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("toro")
                .then(Commands.literal("toggle")
                    .executes(ctx -> toggleToro(ctx.getSource())))
                .then(Commands.literal("run")
                    .then(Commands.argument("program", StringArgumentType.string())
                        .executes(ctx -> runProgram(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "program")))))
                .then(Commands.literal("list")
                    .executes(ctx -> listPrograms(ctx.getSource())))

        );
    }

    private static int toggleToro(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.serverLevel();

        // Check if a Toro already exists for this player
        for (Entity e : level.getEntities(ModEntities.TOROSAURUS.get(), player.getBoundingBox().inflate(64), entity -> true)) {
            if (e.getUUID().equals(player.getUUID())) {
                e.remove(Entity.RemovalReason.DISCARDED);
                source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Toro removed."), false);
                return 1;
            }
        }

        TorosaurusEntity toro = new TorosaurusEntity(ModEntities.TOROSAURUS.get(), level);
        toro.setPos(player.getX(), player.getY(), player.getZ());
        toro.setOwnerUUID(player.getUUID()); // if you have a method for this
        level.addFreshEntity(toro);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Toro summoned."), false);
        return 1;
    }

    private static int runProgram(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.serverLevel();

        // Find the player's Toro
        TorosaurusEntity toro = level.getEntities(ModEntities.TOROSAURUS.get(), player.getBoundingBox().inflate(64),
            e -> e.getUUID().equals(player.getUUID()))
            .stream().findFirst().orElse(null);

        if (toro == null) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Toro not found."));
            return 0;
        }

        // this should always return something,
        // becuase if it's not there it makes a new one
        ToroProgramData data = 
            level.getDataStorage().computeIfAbsent(ToroProgramData.FACTORY, "toro");

        ToroProgram program = data.getProgramsFor(player.getUUID()).get(name);
        if (program == null) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Program not found."));
            return 0;
        }

        toro.runProgram(program);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Program started."), false);
        return 1;
    }

    private static int listPrograms(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.serverLevel();
        ToroProgramData data = level.getDataStorage().computeIfAbsent(ToroProgramData.FACTORY, "toro_programs");

        var map = data.getProgramsFor(player.getUUID());
        if (map.isEmpty()) {
            source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("No programs found."), false);
            return 0;
        }

        for (String name : map.keySet()) {
            source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("• " + name), false);
        }

        return 1;
    }


}

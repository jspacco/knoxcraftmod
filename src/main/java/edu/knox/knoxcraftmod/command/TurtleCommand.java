package edu.knox.knoxcraftmod.command;

import org.slf4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;

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
import edu.knox.knoxcraftmod.BlockDumper;
import edu.knox.knoxcraftmod.data.ToroProgramData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class TurtleCommand 
{
    private static final boolean DUMP_BLOCKS = false;
    private static final Logger LOGGER = LogUtils.getLogger();

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
        if (DUMP_BLOCKS) {
            dispatcher.register(
                Commands.literal("dumpblocks")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> {
                        BlockDumper.dumpAllBlocks(ctx.getSource().getLevel());
                        return 1;
                    }));
        }

    }

    private static int toggleToro(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.serverLevel();

        // Check if a Toro already exists for this player
        // FIXME: is 64 a big enough bounding box?
        for (Entity e : level.getEntities(ModEntities.TOROSAURUS.get(), player.getBoundingBox().inflate(64), entity -> {
            return entity instanceof TorosaurusEntity;}))
        {
            LOGGER.trace("Checking entity of type {} with uuid {}", e.getClass(), e.getUUID());
            TorosaurusEntity t = (TorosaurusEntity)e;
            LOGGER.trace("TorosaurusEntity has owner UUID {}", t.getOwnerUUID());
            if (t.getOwnerUUID().equals(player.getUUID())) {
                
                LOGGER.debug("Removing Toro for uuid "+e.getUUID());
                e.remove(Entity.RemovalReason.DISCARDED);
                source.sendSuccess(() -> Component.literal("Toro removed."), false);
                return 1;
            }
        }
        LOGGER.debug("Spawning new Toro for player uuid "+player.getUUID());

        // TODO: place TORO 1 unit away from the player
        //net.minecraft.core.Direction dir = player.getDirection();
        TorosaurusEntity toro = new TorosaurusEntity(ModEntities.TOROSAURUS.get(), level);
        LOGGER.debug("Spawning new Toro with uuid {}", toro.getUUID());
        toro.setPos(player.getX(), player.getY(), player.getZ());
        toro.setOwnerUUID(player.getUUID()); 
        
        // TODO: figure out how to get the direction correct
        toro.setYRot(player.getYRot());
        toro.setRot(player.getYRot(), player.getXRot());
        toro.updateDirectionFromRotation();

        level.addFreshEntity(toro);

        source.sendSuccess(() -> Component.literal("Toro summoned."), false);
        return 1;
    }

    private static int runProgram(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.serverLevel();

        // Find the player's Toro
        TorosaurusEntity toro = level.getEntities(ModEntities.TOROSAURUS.get(),
            player.getBoundingBox().inflate(64),
            e -> (e instanceof TorosaurusEntity) && ((TorosaurusEntity)e).getOwnerUUID().equals(player.getUUID()))
                .stream().findFirst().orElse(null);

        if (toro == null) {
            source.sendFailure(Component.literal("Toro not found."));
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


}

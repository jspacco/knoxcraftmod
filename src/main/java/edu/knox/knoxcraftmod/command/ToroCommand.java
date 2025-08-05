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
import net.minecraft.world.entity.Entity;
import edu.knox.knoxcraftmod.entity.ModEntities;
import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import edu.knox.knoxcraftmod.data.ToroProgramData;
import net.minecraft.network.chat.Component;

public class ToroCommand 
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Map<UUID, TorosaurusEntity> toroMap = new HashMap<>();
    private static Map<UUID, Map<UUID, TorosaurusEntity>> threadMap = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("toro")
                .requires(source -> source.hasPermission(0))
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
                .then(Commands.literal("help").executes(ctx -> {
                    ctx.getSource().sendSuccess(() ->
                        Component.literal("Toro Commands:\n/toro summon\n/toro list\n/toro stop\n/toro help\n/toro forward|back|up|down|left|right\n/toro run <program>"), false);
                    return 1;
                }))
        );
        // manual movement commands
        LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal("toro")
            .requires(source -> source.hasPermission(0));

        for (String action : List.of("forward", "back",
             "up", "down", "left", "right", 
             "turnleft", "tl", "turnright", "tr")) 
        {
            base.then(Commands.literal(action)
                .executes(ctx -> manualMove(ctx, action)));
        }
        dispatcher.register(base);

        //TODO: restrict to ops only
        dispatcher.register(
            Commands.literal("dumpblocks")
                .requires(source -> source.hasPermission(3))
                .executes(ctx -> {
                try {
                    tools.BlockDumper.dumpBlockModels(ctx.getSource().getLevel());
                } catch (Exception e) {
                    ctx.getSource().sendFailure(Component.literal("failure! " +e.toString()));
                    return 0;
                }
                
                ctx.getSource().sendSuccess(() -> Component.literal("Success"), false);
                return 1;
            }
        ));
    }

    private static int manualMove(CommandContext<CommandSourceStack> ctx, String action) {
        ServerPlayer player = ctx.getSource().getPlayer();

        TorosaurusEntity toro = getMainToro(player.getUUID());
        if (toro == null) {
            // Toro must already exist for a manual move
            ctx.getSource().sendFailure(Component.literal("Toro not found."));
            return 0;
        }

        if (isRunning(player.getUUID())) {
            // Toro must already exist for a manual move
            ctx.getSource().sendFailure(Component.literal("Toro is busy! Use '/toro stop' to stop the Toro first. "));
            return 0;
        }

        toro.moveToro(action);

        ctx.getSource().sendSuccess(() -> Component.literal("Toro moved: " + action), false);
        return 1;
    }

    private static boolean isRunning(UUID playerId) {
        if (!hasMainToro(playerId)) return false;
        TorosaurusEntity toro = getMainToro(playerId);
        // either the main toro is running (serial/single thread)
        // or one of the threads is running
        return toro.isRunning() || threadMap.containsKey(playerId) && 
            threadMap.get(playerId).values().stream().anyMatch(t -> t.isRunning());
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
        if (isRunning(player.getUUID())) {
            source.sendFailure(Component.literal("Toro is busy! Use '/toro stop' to stop the Toro. "));
            return 0;
        }
        moveToroToEntity(toro, player);
        source.sendSuccess(() -> Component.literal("Toro summoned."), false);
        return 1;
    }

    private static int stopToro(CommandSourceStack source)
    {
        ServerPlayer player = source.getPlayer();
        UUID uuid = player.getUUID();
        TorosaurusEntity toro = getMainToro(uuid);
        if (toro == null) {
            source.sendFailure(Component.literal("No Toro to stop. "));
            return 0;
        }
        LOGGER.debug("Stopping toro "+toro.getUUID());
        toro.stop();
        LOGGER.debug("threadMap.keySet(): "+threadMap.keySet());
        if (threadMap.containsKey(uuid)) {
            LOGGER.debug("threadMap.get(uuid):" +threadMap.get(uuid).size());
            // The stream().toList() trick is because I need a shallow copy, 
            // since calling stop() will call back into this method class to remove
            // the thread from threadMap
            for (TorosaurusEntity t : threadMap.get(uuid).values().stream().toList()){
                LOGGER.debug("Stopping thread "+t.getUUID());
                t.stop();
            }
        }
        source.sendSuccess(() -> Component.literal("Toro stopped."), false);
        return 1;
    }

    private static TorosaurusEntity getOrCreateToro(ServerPlayer player, ServerLevel level)
    {
        // get the player's toro, if one exists
        TorosaurusEntity toro = getMainToro(player.getUUID());
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
        LOGGER.debug("Toro fresh entity added");
        
        return toro;
    }

    private static void moveToroToEntity(TorosaurusEntity toro, Entity entity)
    {
        // moves the toro to the entity's location, matches the entities heading as well
        toro.setPos(entity.getX(), entity.getY(), entity.getZ());
        Direction dir = Direction.fromDegrees(entity.getYRot());
        toro.setToroDirection(dir);
    }

    private static void setToro(UUID uuid, TorosaurusEntity toro) {
        toroMap.put(uuid, toro);
    }

    private static TorosaurusEntity getMainToro(UUID uuid) {
        return toroMap.get(uuid);
    }

    private static boolean hasMainToro(UUID uuid) {
        return toroMap.containsKey(uuid);
    }

    private static int runProgram(CommandSourceStack source, String programName) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.serverLevel();

        TorosaurusEntity toro = getMainToro(player.getUUID());
        
        if (toro == null) {
            source.sendFailure(Component.literal("First summon your Toro with '/toro summon'"));
            return 0;
        }

        if (isRunning(player.getUUID())) {
            source.sendFailure(Component.literal("Toro is busy! Wait or stop '/toro stop' "));
            return 0;
        }

        // this should always return something,
        // becuase if it's not there it makes a new one
        ToroProgramData data = ToroProgramData.get(level);

        String playerName = player.getGameProfile().getName();
        LOGGER.debug("Game Profile name is "+playerName);
        ToroProgram program = data.getProgramsFor(player.getGameProfile().getName()).get(programName);
        if (program == null) {
            source.sendFailure(Component.literal(String.format("Program '%s' not found.", programName)));
            return 0;
        }

        if (program instanceof SerialToroProgram serial) {
            // serial (single thread) program
            toro.runProgram(serial.getInstructions());
        } else if (program instanceof ParallelToroProgram parallel){
            // Parallel
            // create and run toro thread for each set of instructions
            for (List<Instruction> instructions : parallel.getThreads()) {
                TorosaurusEntity thread = spawnToroThread(player, level, toro);
                thread.setIsThread(true);
                addToroThread(player.getUUID(), thread);
                // start running the list of instructions
                thread.runProgram(instructions);
            }
        } else {
            LOGGER.error("Program is type {} which is not serial or parallel", program.getClass());
            source.sendFailure(Component.literal("Error! Program is type "+program.getClass()+", not serial or parallel; this should never happen"));
            return 0;
        }

        
        source.sendSuccess(() -> Component.literal("Program "+programName+"loaded!"), false);
        return 1;
    }

    private static TorosaurusEntity spawnToroThread(ServerPlayer player, ServerLevel level, Entity entity) {
        TorosaurusEntity toro = new TorosaurusEntity(ModEntities.TOROSAURUS.get(), level);
        LOGGER.debug("Spawning new Toro with uuid {}", toro.getUUID());
        // add toro thread to our map
        addToroThread(player.getUUID(), toro);
        // set owner to player
        toro.setOwnerUUID(player.getUUID());
        // move to match the location of the entity (which is the original toro)
        moveToroToEntity(toro, entity);
        level.addFreshEntity(toro);
        return toro;
    }

    private static void addToroThread(UUID uuid, TorosaurusEntity toro) {
        threadMap.computeIfAbsent(uuid, id -> new HashMap<UUID, TorosaurusEntity>()).put(toro.getUUID(), toro);
    }

    public static void threadEnded(UUID playerId, TorosaurusEntity entity) {
        // remove a thread from our mapping
        LOGGER.debug("threadEnded {} {}", playerId, entity.isThread());
        if (threadMap.containsKey(playerId)) {
            threadMap.get(playerId).remove(entity.getUUID());
        }
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

        source.sendSuccess(() -> Component.literal("Programs:"), false);
        for (String name : map.keySet()) {
            LOGGER.debug("Program name: "+name);
            source.sendSuccess(() -> Component.literal("-> " + name +": "+map.get(name).getDescription()), false);
        }

        return 1;
    }

    public static void removeToroMapping(UUID uuid) {
        if (uuid != null){
            toroMap.remove(uuid);
            threadMap.remove(uuid);
        }
    }

    public static void logout(ServerPlayer player) {
        TorosaurusEntity toro = toroMap.get(player.getUUID());
        if (toro != null) {
            toro.discard();
            toroMap.remove(player.getUUID());
        }
        if (threadMap.containsKey(player.getUUID())) {
            threadMap.get(player.getUUID()).forEach((uuid, t) -> {
                t.discard();
            });
            threadMap.remove(player.getUUID());
        }
    }

}

package edu.knox.knoxcraftmod.command;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.brigadier.Command;
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
import net.minecraft.world.entity.EntityType;
import edu.knox.knoxcraftmod.entity.TerpTurtle;
import edu.knox.knoxcraftmod.util.Msg;
import edu.knox.knoxcraftmod.data.TerpProgramData;

public class TerpCommand 
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Map<UUID, TerpTurtle> terpMap = new HashMap<>();
    private static Map<UUID, Map<UUID, TerpTurtle>> threadMap = new HashMap<>();

    private static boolean hasPermission(CommandSourceStack source, int permissionLevel) {
        return source.hasPermission(permissionLevel);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("terp")
                .requires(source -> hasPermission(source, 0))
                .then(Commands.literal("summon")
                    .executes(ctx -> summonTerp(ctx.getSource())))
                .then(Commands.literal("run")
                    .then(Commands.argument("programName", StringArgumentType.word())
                    // if only <programName> provided
                    .executes(ctx -> {
                        ServerPlayer executor = ctx.getSource().getPlayerOrException();
                        String programName = StringArgumentType.getString(ctx, "programName");
                        return runProgram(ctx.getSource(), programName, executor.getGameProfile().getName());
                    })
                    // if <programName> and <ownerName> provided
                    .then(Commands.argument("ownerName", StringArgumentType.word())
                        .executes(ctx -> {
                            String programName = StringArgumentType.getString(ctx, "programName");
                            String ownerName = StringArgumentType.getString(ctx, "ownerName");
                            return runProgram(ctx.getSource(), programName, ownerName);
                        }))))
                .then(Commands.literal("list")
                    .executes(ctx -> listPrograms(ctx.getSource())))
                .then(Commands.literal("stop")
                    .executes(ctx -> stopTerp(ctx.getSource())))
                .then(Commands.literal("help").executes(ctx -> {
                    Msg.reply(ctx.getSource(), 
                        "Terp Commands:\n/terp summon\n/terp list\n/terp stop\n/terp help\n/terp forward|back|up|down|left|right\n/terp run <program>", 
                        false);
                    return Command.SINGLE_SUCCESS;
                }))
        );
        // manual movement commands
        LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal("terp")
            .requires(source -> hasPermission(source, 0));

        for (String action : List.of("forward", "back",
             "up", "down", "left", "right", 
             "turnleft", "tl", "turnright", "tr")) 
        {
            base.then(Commands.literal(action)
                .executes(ctx -> manualMove(ctx, action)));
        }
        dispatcher.register(base);

        //TODO: restrict to ops only
        // dispatcher.register(
        //     Commands.literal("dumpblocks")
        //         .requires(source -> hasPermission(source, 4))
        //         .executes(ctx -> {
        //         try {
        //             tools.BlockDumper.dumpBlockModels(ctx.getSource().getLevel());
        //         } catch (Exception e) {
        //             Msg.fail(ctx.getSource(), "failure! " +e.toString());
        //             return 0;
        //         }
                
        //         Msg.reply(ctx.getSource(), "Success", false);
        //         //ctx.getSource().sendSuccess(() -> Component.literal("Success"), false);
        //         return 1;
        //     }
        // ));
    }

    private static int manualMove(CommandContext<CommandSourceStack> ctx, String action) {
        ServerPlayer player = ctx.getSource().getPlayer();

        TerpTurtle terp = getMainTerp(player.getUUID());
        if (terp == null) {
            // Terp must already exist for a manual move
            Msg.fail(ctx.getSource(), "Terp not found.");
            return 0;
        }

        if (isRunning(player.getUUID())) {
            // Terp must already exist for a manual move
            Msg.fail(ctx.getSource(), "Terp is busy! Use '/terp stop' to stop the Terp first. ");
            return 0;
        }

        terp.moveTerp(action);

        Msg.reply(ctx.getSource(), "Terp moved: " + action, false);
        return Command.SINGLE_SUCCESS;
    }

    private static boolean isRunning(UUID playerId) {
        if (!hasMainTerp(playerId)) return false;
        TerpTurtle terp = getMainTerp(playerId);
        // either the main terp is running (serial/single thread)
        // or one of the threads is running
        return terp.isRunning() || threadMap.containsKey(playerId) && 
            threadMap.get(playerId).values().stream().anyMatch(t -> t.isRunning());
    }

    private static int summonTerp(CommandSourceStack source)
    {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        if (player.getY() >= level.getMaxY()) {
            Msg.fail(source, "Cannot summon Terp above max build height. ");
            return 0;
        }
        TerpTurtle terp = getOrCreateTerp(player, level);
        if (isRunning(player.getUUID())) {
            Msg.fail(source, "Terp is busy! Use '/terp stop' to stop the Terp. ");
            return 0;
        }
        moveTerpToEntity(terp, player);
        Msg.send(player, "Terp Summoned. ", true);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopTerp(CommandSourceStack source)
    {
        ServerPlayer player = source.getPlayer();
        UUID uuid = player.getUUID();
        TerpTurtle terp = getMainTerp(uuid);
        if (terp == null) {
            Msg.send(player, "No Terp to stop.", true);
            return 0;
        }
        LOGGER.debug("Stopping terp "+terp.getUUID());
        terp.stop();
        LOGGER.debug("threadMap.keySet(): "+threadMap.keySet());
        if (threadMap.containsKey(uuid)) {
            LOGGER.debug("threadMap.get(uuid):" +threadMap.get(uuid).size());
            // The stream().toList() trick is because I need a shallow copy, 
            // since calling stop() will call back into this method class to remove
            // the thread from threadMap
            for (TerpTurtle t : threadMap.get(uuid).values().stream().toList()){
                LOGGER.debug("Stopping thread "+t.getUUID());
                t.stop();
            }
        }

        Msg.send(player, "Terp stopped.", true);
        return Command.SINGLE_SUCCESS;
    }

    private static TerpTurtle getOrCreateTerp(ServerPlayer player, ServerLevel level)
    {
        // get the player's terp, if one exists
        TerpTurtle terp = getMainTerp(player.getUUID());
        if (terp != null) return terp;

        // can't find the terp, so make a new one
        terp = new TerpTurtle(EntityType.TURTLE, level);
        LOGGER.info("Spawning new Terp with uuid {} and type {}", terp.getUUID(), terp.getType().toString());
        
        setTerp(player.getUUID(), terp);
        terp.setOwnerUUID(player.getUUID());
        // It feels like I should call moveTerpToPlayer(terp, player) here, 
        // but I know it will get called anytime I summon a terp, so I would be doing
        // that twice. But it still feels like I should set the location here.

        // add Terp to the level, be sure to add on the server
        if (!level.isClientSide()) {
            level.addFreshEntity(terp);
            LOGGER.debug("Terp fresh entity added");
        }

        long count = level.getEntities(EntityType.TURTLE, e -> true).size();
        LOGGER.info("Turtles in world: {}", count);
        
        
        return terp;
    }

    private static void moveTerpToEntity(TerpTurtle terp, Entity entity)
    {
        // moves the terp to the entity's location, matches the entities heading as well
        terp.setPos(entity.getX(), entity.getY(), entity.getZ());
        Direction dir = Direction.fromDegrees(entity.getYRot());
        terp.setTerpDirection(dir);
    }

    private static void setTerp(UUID uuid, TerpTurtle terp) {
        terpMap.put(uuid, terp);
    }

    private static TerpTurtle getMainTerp(UUID uuid) {
        return terpMap.get(uuid);
    }

    private static boolean hasMainTerp(UUID uuid) {
        return terpMap.containsKey(uuid);
    }

    private static int runProgram(CommandSourceStack source, String programName, String playerName) {
        // executor is the player running the terp command
        // may not be the person owning the program to be run
        // students may not own minecraft and may borrow their friend's accounts
        ServerPlayer executor = source.getPlayer();
        ServerLevel level = source.getLevel();

        TerpTurtle terp = getMainTerp(executor.getUUID());
        
        if (terp == null) {
            Msg.fail(source, "First summon your Terp with '/terp summon'");
            return 0;
        }

        if (isRunning(executor.getUUID())) {
            Msg.fail(source, "Terp is busy! Wait or stop with '/terp stop' ");
            return 0;
        }

        // this should always return something,
        // becuase if it's not there it makes a new one
        TerpProgramData data = TerpProgramData.get(level);

        //String playerName = executor.getGameProfile().getName();
        LOGGER.debug("Looking for {} for player {} ", programName, playerName);
        Map<String, TerpProgram> programs = data.getProgramsFor(playerName);
        if (programs.isEmpty()) {
            Msg.fail(source, format("No programs found for user '%s'", playerName));
            return 0;
        }
        TerpProgram program = programs.get(programName);
        if (program == null) {
            Msg.fail(source, format("Program '%s' not found", programName));
            return 0;
        }

        if (program instanceof SerialTerpProgram serial) {
            // serial (single thread) program
            terp.runProgram(serial.getInstructions());
        } else if (program instanceof ParallelTerpProgram parallel){
            // Parallel
            // create and run terp thread for each set of instructions
            for (List<Instruction> instructions : parallel.getThreads()) {
                TerpTurtle thread = spawnTerpThread(executor, level, terp);
                thread.setIsThread(true);
                addTerpThread(executor.getUUID(), thread);
                // start running the list of instructions
                thread.runProgram(instructions);
            }
        } else {
            LOGGER.error("Program is type {} which is not serial or parallel", program.getClass());
            Msg.fail(source, "Error! Program is type "+program.getClass()+", not serial or parallel; this should never happen.");
            return 0;
        }

        Msg.send(executor, format("Program %s loaded.", programName), false);
        return Command.SINGLE_SUCCESS;
    }

    private static TerpTurtle spawnTerpThread(ServerPlayer player, ServerLevel level, Entity entity) {
        TerpTurtle terp = new TerpTurtle(EntityType.TURTLE, level);
        LOGGER.debug("Spawning new Terp with uuid {} and class {}", terp.getUUID(), terp.getClass());
        // add terp thread to our map
        addTerpThread(player.getUUID(), terp);
        // set owner to player
        terp.setOwnerUUID(player.getUUID());
        // move to match the location of the entity (which is the original terp)
        moveTerpToEntity(terp, entity);
        level.addFreshEntity(terp);
        return terp;
    }

    private static void addTerpThread(UUID uuid, TerpTurtle terp) {
        threadMap.computeIfAbsent(uuid, id -> new HashMap<UUID, TerpTurtle>()).put(terp.getUUID(), terp);
    }

    public static void threadEnded(UUID playerId, TerpTurtle entity) {
        // remove a thread from our mapping
        LOGGER.debug("threadEnded {} {}", playerId, entity.isThread());
        if (threadMap.containsKey(playerId)) {
            threadMap.get(playerId).remove(entity.getUUID());
        }
    }

    private static int listPrograms(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        TerpProgramData data = TerpProgramData.get(level);

        String playerName = player.getGameProfile().getName();
        LOGGER.debug("Game Profile playerName is "+playerName);
        var map = data.getProgramsFor(playerName);
        if (map.isEmpty()) {
            Msg.send(player, "No programs found.", true);
            return 0;
        }

        Msg.send(player, "Programs: ", false);
        for (String name : map.keySet()) {
            LOGGER.debug("Program name: "+name);
            Msg.send(player, "-> " + name +": "+map.get(name).getDescription(), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void removeTerpMapping(UUID uuid) {
        if (uuid != null){
            terpMap.remove(uuid);
            threadMap.remove(uuid);
        }
    }

    public static void logout(ServerPlayer player) {
        TerpTurtle terp = terpMap.get(player.getUUID());
        if (terp != null) {
            terp.discard();
            terpMap.remove(player.getUUID());
        }
        if (threadMap.containsKey(player.getUUID())) {
            threadMap.get(player.getUUID()).forEach((uuid, t) -> {
                t.discard();
            });
            threadMap.remove(player.getUUID());
        }
    }

}

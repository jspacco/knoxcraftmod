package edu.knox.knoxcraftmod.entity;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.command.Direction;
import edu.knox.knoxcraftmod.command.Instruction;
import edu.knox.knoxcraftmod.command.TerpCommand;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class TerpTurtle extends Turtle {

    public TerpTurtle(EntityType<? extends Turtle> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.MAX_Y = pLevel.getMaxY();
        // terps can fly
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                // Do nothing — prevents automatic head turning
            }
        };
    }

    private static final int SUPERFLAT_GROUND_LEVEL = -60;
    private final int MAX_Y;
    private static final String SET_BLOCK = "setBlock";

    private static final Logger LOGGER = LogUtils.getLogger();
    
    private UUID ownerUUID;
    
    // is this a thread?
    private boolean isThread;

    private boolean isRunning = false;
    private List<Instruction> program;
    private int ip = 0;
    
    // The direction (NORTH, SOUTH, EAST, WEST) is encoded as an int
    // that is always stored on the server. We always get the int from
    // the server and never update it on the client side.
    private Direction direction = Direction.SOUTH;
    

    @Override
    protected void updateControlFlags() {
        // Prevent setting flags like FLAG_MOVING that trigger head/body turns
    }

    public void setTerpDirection(Direction newDirection) {
        if (this.direction != newDirection) {
            this.direction = newDirection;
            float yaw = yawFromDirection(newDirection);
            this.setYRot(yaw);
            this.setYHeadRot(yaw);
            this.setYBodyRot(yaw);
        }
    }

    private float yawFromDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> 180f;
            case EAST -> -90f;
            case SOUTH -> 0f;
            case WEST -> 90f;
            default -> 0f;
        };
    }

    public Direction getTerpDirection() {
        return direction;
    }

    public void runProgram(List<Instruction> instructions)
    {
        this.program = instructions;
        this.ip = 0;
        this.isRunning = true;
    }


    @Override
    public void tick() {
        super.tick();

        this.setGlowingTag(true);
        if (this.level().isClientSide && this.tickCount % 20 == 0) {
            LOGGER.debug("Turtle (client) tick at " + this.getX() + " " + this.getY() + " " + this.getZ());
        }
        if (this.level().isClientSide && this.tickCount % 40 == 0) {
            LOGGER.debug("Entity class: " + this.getClass().getName());
        }

        if (this.level().isClientSide) return;


        if (!level().isClientSide) {
            // Server sets Y rotation
            setYRot(getTerpDirection().toYaw());
            setYHeadRot(getYRot());
            setYBodyRot(getYRot());
            // This should never happen!
            // but if it somehow does, move the Terp to the highest legal location
            if (this.getY() >= MAX_Y) {
                this.setPos(this.getX(), MAX_Y - 1, this.getZ());
            }
        }

        // program ran to completion; stop it
        if (isRunning && program != null && ip >= program.size()) {
            this.stop();
            return;
        }

        if (program == null || ip >= program.size() || !isRunning) return;

        // fetch the next instruction
        Instruction instr = program.get(ip++);

        LOGGER.trace("Running instruction #{} which is {}", ip, instr);

        // setBlock
        if (instr.command.equals(SET_BLOCK)) {
            if (this.level() instanceof ServerLevel serverLevel) {
                // block types are stored as: "minecraft:dirt"
                HolderLookup.Provider regs = this.level().registryAccess();
                HolderGetter<Block> blocks = regs.lookupOrThrow(Registries.BLOCK);
                ResourceLocation loc = ResourceLocation.parse(instr.blockType);
                ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, loc);
                Block block = blocks.getOrThrow(key).value();

                if (block != null) {
                    BlockPos current = blockPosition();
                    serverLevel.setBlock(current, block.defaultBlockState(), 3);
                } else {
                    LOGGER.warn("Unknown block type: {}", instr.blockType);
                }
            }
            // setblock is done
            return;
        }

        if (!List.of("forward", "back", "left", "right", 
            "up", "down", "turnleft", "turnright", 
            "tl", "tr", "nop").contains(instr.command))
        {
            LOGGER.warn("unknown command: "+instr.command);
            return;
        }

        // otherwise it was a move command
        moveTerp(instr.command);
    }

    public void moveTerp(String command)
    {
        BlockPos current = blockPosition();

        BlockPos target = switch (command) {
            // nop command is a no-op
            case "nop" -> current;
            case "forward" -> offset(current, getTerpDirection());
            case "up" -> {
                if (current.getY() >= MAX_Y) {
                    yield current;
                }
                yield current.above();
            }
            case "down" -> {
                // can't set blocks below the bottom
                if (current.getY() <= SUPERFLAT_GROUND_LEVEL) {
                    yield current;
                }
                yield current.below();
            }
            case "back" -> offset(current, getTerpDirection().opposite());
            case "left" -> offset(current, getTerpDirection().turnLeft());
            case "right" -> offset(current, getTerpDirection().turnRight());
            default -> current;
        };

        switch (command) {
            case "forward", "up", "down", "back", "left", "right" -> {
                // allow overwriting blocks
                setPos(Vec3.atBottomCenterOf(target));
            }
            
            case "turnleft", "tl" -> {
                setTerpDirection(getTerpDirection().turnLeft());
            }
            case "turnright", "tr" -> {
                setTerpDirection(getTerpDirection().turnRight());
            }
        }
    }

    private BlockPos offset(BlockPos pos, Direction dir) {
        return switch (dir) {
            case NORTH -> pos.north();
            case SOUTH -> pos.south();
            case EAST -> pos.east();
            case WEST -> pos.west();
        };
    }

    @Override
    protected void registerGoals() {
        // don't register any AI goals
    }

    public static AttributeSupplier.Builder createAttributes() {
        // I can make this mob faster here
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    // Save and load the Terp
    // We know the UUID because this requires the person to be logged in
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            saveUUID(ownerUUID, tag, "owner");
        }
    }

    // the NBT api seems to have changed between 1.21.1 and 1.21.5
    // we've lost get/save UUID method, and I guess we get back Optional<T> now?
    // I'm abstracting away save/load UUID into static methods
    // these hopefully will act as shims in case the API changes again
    private static void saveUUID(UUID uuid, CompoundTag tag, String key) {
        tag.putString(key, uuid.toString());
    }

    private static UUID loadUUID(CompoundTag tag, String key) {
        if (tag.contains(key)) {
            return UUID.fromString(tag.getString(key).get());
        }
        return null;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUUID = loadUUID(tag, "owner");
    }

    @Override
    public boolean isInvisible() {
        return false;
    }


    // change visibility
    @Override
    public void setRot(float pYRot, float pXRot) {
        super.setRot(pYRot, pXRot);
    }

    @Override
    public boolean isPushable() {
        // can't be pushed
        return false;
    }

    @Override
    public boolean isPickable() {
        // can't be targeted with mouse
        return false; 
    }

    @Override
    public boolean isAffectedByFluids() {
        // unaffected by fluids
        return false;
    }

    @Override
    public boolean isInWall() {
        // never gets stuck in a wall (I think?)
        return false;
    }

    @Override
    protected boolean isImmobile() {
        // not immobile
        return false;
    }

    @Override
    public boolean isCurrentlyGlowing() {
        // always glowing
        return true;
    }

    @Override
    public void turn(double yRot, double xRot) {
        // Suppress player input or AI turning
    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 target) {
        // Suppress automatic look-at behavior
    }


    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (!level().isClientSide) {
            this.setYRot(getTerpDirection().toYaw());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        LOGGER.debug("stop() called, isThread {}", isThread);
        isRunning = false;
        program = null;
        ip = -1;
        if (this.isThread()){
            LOGGER.debug("discarding thread "+this.getUUID());
            // calling stop() on a thread removes it from the player's threads
            TerpCommand.threadEnded(ownerUUID, this);
            // GPS Sensei says it's fine to call this twice
            this.discard();
        }
    }

    public boolean isThread() {
        return isThread;
    }

    public void setIsThread(boolean isThread) {
        this.isThread = isThread;
    }
}

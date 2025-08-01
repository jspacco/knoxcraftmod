package edu.knox.knoxcraftmod.entity.custom;

import edu.knox.knoxcraftmod.command.Direction;
import edu.knox.knoxcraftmod.command.Instruction;
import edu.knox.knoxcraftmod.command.ToroProgram;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

public class TorosaurusEntity extends Mob {
    private static final int SUPERFLAT_GROUND_LEVEL = -60;
    private final int MAX_Y;
    private static final String SET_BLOCK = "setBlock";

    private static final Logger LOGGER = LogUtils.getLogger();
    
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    
    private UUID ownerUUID;

    private boolean isRunning = false;
    private List<Instruction> program;
    private int ip = 0;
    
    // The direction (NORTH, SOUTH, EAST, WEST) is encoded as an int
    // that is always stored on the server. We always get the int from
    // the server and never update it on the client side.
    private static final EntityDataAccessor<Integer> DIRECTION =
        SynchedEntityData.defineId(TorosaurusEntity.class, EntityDataSerializers.INT);


    public TorosaurusEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.MAX_Y = level.getMaxBuildHeight();
        // toros can fly
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                // Do nothing — prevents automatic head turning
            }
        };

    }
    @Override
    protected void updateControlFlags() {
        // Prevent setting flags like FLAG_MOVING that trigger head/body turns
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // default to south (Minecraft defaults to south)
        builder.define(DIRECTION, Direction.SOUTH.ordinal());
    }

    public void setToroDirection(Direction direction) {
        int ordinal = direction.ordinal();
        if (entityData.get(DIRECTION) != ordinal) {
            entityData.set(DIRECTION, ordinal);
        }
    }


    public Direction getToroDirection() {
        return Direction.fromOrdinal(this.entityData.get(DIRECTION));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (key.equals(DIRECTION)) {
            LOGGER.debug("HEYHEY Toro direction updated on client: {}", getToroDirection());
            float yaw = getToroDirection().toYaw();
            setYRot(yaw);
            setYHeadRot(yaw);
            setYBodyRot(yaw);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // invulnerable turtle
        return false; 
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 40;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    public void runProgram(ToroProgram program)
    {
        this.program = program.getInstructions();
        this.ip = 0;
        this.isRunning = true;
    }


    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()){
            this.setupAnimationStates();
            return;
        }

        if (!level().isClientSide) {
            // Server sets Y rotation
            setYRot(getToroDirection().toYaw());
            setYHeadRot(getYRot());
            setYBodyRot(getYRot());
            // This should never happen!
            // but if it somehow does, move the Toro to the highest legal location
            if (this.getY() >= MAX_Y) {
                this.setPos(this.getX(), MAX_Y - 1, this.getZ());
            }
        }

        // program ran to completion; stop it
        if (isRunning && ip >= program.size()) this.stop();

        if (program == null || ip >= program.size() || !isRunning) return;

        // fetch the next instruction
        Instruction instr = program.get(ip++);

        LOGGER.debug("Running instruction #{} which is {}", ip, instr);

        // setBlock
        if (instr.command.equals(SET_BLOCK)) {
            if (this.level() instanceof ServerLevel serverLevel) {
                var blockRegistry = serverLevel.registryAccess()
                    .registryOrThrow(Registries.BLOCK);

                // block types are stored as: "minecraft:dirt"
                String[] tmp = instr.blockType.split(":");
                if (tmp.length != 2) {
                    LOGGER.error("Unknown block type: {}", instr.blockType);
                    return;
                }
                String pNamespace = tmp[0];
                String pPath = tmp[1];
                
                Block block = blockRegistry.getOptional(ResourceLocation
                    .fromNamespaceAndPath(pNamespace, pPath))
                    .orElse(null);

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
            "tl", "tr").contains(instr.command))
        {
            LOGGER.warn("unknown command: "+instr.command);
            return;
        }

        // otherwise it was a move command
        moveToro(instr.command);
    }

    public void moveToro(String command)
    {
        BlockPos current = blockPosition();

        BlockPos target = switch (command) {
            case "forward" -> offset(current, getToroDirection());
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
            case "back" -> offset(current, getToroDirection().opposite());
            case "left" -> offset(current, getToroDirection().turnLeft());
            case "right" -> offset(current, getToroDirection().turnRight());
            default -> current;
        };

        switch (command) {
            case "forward", "up", "down", "back", "left", "right" -> {
                // allow overwriting blocks
                setPos(Vec3.atBottomCenterOf(target));
            }
            
            case "turnleft", "tl" -> {
                setToroDirection(getToroDirection().turnLeft());
            }
            case "turnright", "tr" -> {
                setToroDirection(getToroDirection().turnRight());
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
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    // Save and load the Toro
    // We know the UUID because this requires the person to be logged in
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID("owner", ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("owner")) {
            ownerUUID = tag.getUUID("owner");
        }
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
            this.setYRot(getToroDirection().toYaw());
        }
    }
    public boolean isRunning() {
        return isRunning;
    }
    public void stop() {
        isRunning = false;
        program = null;
        ip = -1;
    }

}

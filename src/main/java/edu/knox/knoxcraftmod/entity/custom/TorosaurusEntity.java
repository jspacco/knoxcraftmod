package edu.knox.knoxcraftmod.entity.custom;

import edu.knox.knoxcraftmod.command.Direction;
import edu.knox.knoxcraftmod.command.Instruction;
import edu.knox.knoxcraftmod.command.ToroProgram;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

public class TorosaurusEntity extends Mob {
    private static final String SET_BLOCK = "setBlock";
    private static final Logger LOGGER = LogUtils.getLogger();
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private UUID ownerUUID;

    private List<Instruction> program;
    private int ip = 0;
    private Direction direction = Direction.NORTH;

    public TorosaurusEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        // toros can fly
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
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
        
        LOGGER.debug("runProgram facing " +direction);
        // get the server
        Player player = this.level().
            getPlayerByUUID(
                UUID.fromString("380df991-f603-344c-a090-369bad2a924a"));
        
        LOGGER.debug("player facing " + player.getDirection());
    }


    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()){
            this.setupAnimationStates();
            return;
        }

        if (program == null || ip >= program.size()) return;

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
            "up", "down", "turnLeft", "turnRight", 
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
            case "forward" -> offset(current, direction);
            case "up" -> current.above();
            case "down" -> {
                // can't set blocks below the bottom
                if (current.getY() < 1) {
                    LOGGER.debug("y value is "+current.getY());
                    yield current;
                }
                yield current.below();
            }
            case "back" -> offset(current, direction.opposite());
            case "left" -> offset(current, direction.turnLeft());
            case "right" -> offset(current, direction.turnRight());
            default -> current;
        };

        switch (command) {
            case "forward", "up", "down", "back", "left", "right" -> {
                // allow overwriting blocks
                setPos(Vec3.atBottomCenterOf(target));
            }
            
            // TODO: update look of turtle
            case "turnLeft", "tl" -> {
                direction = direction.turnLeft();
            }
            case "turnRight", "tr" -> {
                direction = direction.turnRight();
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

    public void updateDirectionFromRotation() {
        float rot = Mth.wrapDegrees(getYRot());
        if (rot >= -45 && rot < 45) {
            direction = Direction.SOUTH;
        } else if (rot >= 45 && rot < 135) {
            direction = Direction.WEST;
        } else if (rot >= -135 && rot < -45) {
            direction = Direction.EAST;
        } else {
            direction = Direction.NORTH;
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
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (!level().isClientSide) {
            updateDirectionFromRotation();
        }
    }

}

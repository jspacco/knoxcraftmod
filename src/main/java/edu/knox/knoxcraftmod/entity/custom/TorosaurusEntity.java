package edu.knox.knoxcraftmod.entity.custom;

import edu.knox.knoxcraftmod.command.Direction;
import edu.knox.knoxcraftmod.command.Instruction;
import edu.knox.knoxcraftmod.command.ToroProgram;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class TorosaurusEntity extends Mob {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private UUID ownerUUID;

    private List<Instruction> program;
    private int ip = 0;
    private Direction direction = Direction.NORTH;

    public TorosaurusEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false; // invulnerable turtle
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

    }


    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()){
            this.setupAnimationStates();
        }

        if (level().isClientSide) return;

        
        if (program == null || ip >= program.size()) return;

        Instruction instr = program.get(ip++);
        BlockPos current = blockPosition();
        BlockPos target = switch (instr.command) {
            case "forward" -> offset(current, direction);
            case "up" -> current.above();
            case "down" -> current.below();
            default -> current;
        };

        switch (instr.command) {
            case "forward", "up", "down" -> {
                if (level().getBlockState(target).isAir()) {
                    //TODO: move, then place the
                    //this.move???
                    setPos(Vec3.atBottomCenterOf(target));
                }
            }
            
            case "turnLeft" -> direction = direction.turnLeft();
            case "turnRight" -> direction = direction.turnRight();
        }
        // Don't place blocks yet
        //if (instr)
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
        // don't register any goals
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }
}

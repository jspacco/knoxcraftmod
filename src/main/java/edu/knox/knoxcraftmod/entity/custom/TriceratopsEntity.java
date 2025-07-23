package edu.knox.knoxcraftmod.entity.custom;

import edu.knox.knoxcraftmod.entity.ModEntities;
import edu.knox.knoxcraftmod.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TriceratopsEntity extends Animal {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public TriceratopsEntity(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        // TODO: is this where the custom AI stuff will happen?
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().
            add(Attributes.MAX_HEALTH, 30).
            add(Attributes.MOVEMENT_SPEED, 0.350).
            add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return pStack.is(ModItems.ALEXANDRITE.get());
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return ModEntities.TRICERATOPS.get().create(pLevel);
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 40;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }
    
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()){
            this.setupAnimationStates();
        }
    }
}

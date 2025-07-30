package edu.knox.knoxcraftmod.entity;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KnoxcraftMod.MODID);

    public static final RegistryObject<EntityType<TorosaurusEntity>> TOROSAURUS =
        ENTITY_TYPES.register("torosaurus", () -> EntityType.Builder.of(TorosaurusEntity::new, MobCategory.CREATURE)
            .sized(1f, 1f).build("torosaurus"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

package edu.knox.knoxcraftmod.item;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, KnoxcraftMod.MODID);
    
    
    public static final RegistryObject<Item> ALEXANDRITE = ITEMS.register("alexandrite",
        () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TRICERATOPS_SPAWN_EGG = ITEMS.register("triceratops_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.TRICERATOPS, 0x53524b, 0xdac741, new Item.Properties()));
    
    public static final RegistryObject<Item> TOROSAURUS_SPAWN_EGG = ITEMS.register("torosaurus_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.TOROSAURUS, 0x53524b, 0xdac742, new Item.Properties()));

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
    
}

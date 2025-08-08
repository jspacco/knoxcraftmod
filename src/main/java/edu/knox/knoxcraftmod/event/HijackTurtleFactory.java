package edu.knox.knoxcraftmod.event;

import java.lang.reflect.Field;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.entity.TerpTurtle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HijackTurtleFactory {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                // Forge obfuscated name for the entity factory field in EntityType
                Field factoryField = ObfuscationReflectionHelper.findField(EntityType.class, "factory");
                factoryField.setAccessible(true);
                factoryField.set(EntityType.TURTLE, (EntityType.EntityFactory<Turtle>) TerpTurtle::new);
                LOGGER.info("Successfully hijacked turtle entity creation.");
            } catch (Exception e) {
                LOGGER.error("Failed to hijack turtle entity creation", e);
            }
        });
    }
}


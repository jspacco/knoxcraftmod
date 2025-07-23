package edu.knox.knoxcraftmod.event;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.entity.ModEntities;
import edu.knox.knoxcraftmod.entity.client.TorosaurusModel;
import edu.knox.knoxcraftmod.entity.client.TriceratopsModel;
import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import edu.knox.knoxcraftmod.entity.custom.TriceratopsEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KnoxcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(TriceratopsModel.LAYER_LOCATION, TriceratopsModel::createBodyLayer);
        event.registerLayerDefinition(TorosaurusModel.LAYER_LOCATION, TorosaurusModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void regsiterAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TRICERATOPS.get(), TriceratopsEntity.createAttributes().build());
        event.put(ModEntities.TOROSAURUS.get(), TorosaurusEntity.createAttributes().build());
    }
    
}

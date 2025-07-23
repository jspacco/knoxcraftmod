package edu.knox.knoxcraftmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.entity.custom.TorosaurusEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class TorosaurusRenderer extends MobRenderer<TorosaurusEntity, TorosaurusModel<TorosaurusEntity>> 
{    
    public TorosaurusRenderer(Context pContext) {
        super(pContext, new TorosaurusModel<>(pContext.bakeLayer(TorosaurusModel.LAYER_LOCATION)), 0.85f);
    }

    

    @Override
    public ResourceLocation getTextureLocation(TorosaurusEntity pEntity) {
        return ResourceLocation.fromNamespaceAndPath(KnoxcraftMod.MODID, "textures/entity/torosaurus/torosaurus.png");
    }

    @Override
    public void render(TorosaurusEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        // scale down the toro to similar size of a dog
        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        
    }
    
    
}


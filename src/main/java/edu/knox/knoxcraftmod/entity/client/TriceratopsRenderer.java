package edu.knox.knoxcraftmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;

import edu.knox.knoxcraftmod.KnoxcraftMod;
import edu.knox.knoxcraftmod.entity.custom.TriceratopsEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TriceratopsRenderer extends MobRenderer<TriceratopsEntity, TriceratopsModel<TriceratopsEntity>> {

    public TriceratopsRenderer(Context pContext) {
        super(pContext, new TriceratopsModel<>(pContext.bakeLayer(TriceratopsModel.LAYER_LOCATION)), 0.85f);
    }

    

    @Override
    public ResourceLocation getTextureLocation(TriceratopsEntity pEntity) {
        return ResourceLocation.fromNamespaceAndPath(KnoxcraftMod.MODID, "textures/entity/triceratops/triceratops_gray.png");
    }

    @Override
    public void render(TriceratopsEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        // if (pEntity.isBaby()) {
        //     pPoseStack.scale(0.5f, 0.5f, 0.5f);
        // } else {
        //     pPoseStack.scale(1f, 1f, 1f);
        // }

        // scale down the triceratops to similar size of a dog
        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        
    }
    
    
}

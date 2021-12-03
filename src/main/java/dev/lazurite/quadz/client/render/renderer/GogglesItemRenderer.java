package dev.lazurite.quadz.client.render.renderer;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.model.GogglesItemModel;
import dev.lazurite.quadz.common.item.GogglesItem;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.GeoArmorRendererFactory;

public class GogglesItemRenderer extends GeoArmorRenderer<GogglesItem> {
    // old
//    public GogglesItemRenderer(GeoArmorRendererFactory.Context ctx) {
//        super(new GogglesItemModel(), ctx, new ModelLayerLocation(new ResourceLocation(Quadz.MODID, "goggles"), "main"));
//        this.headBone = "goggles";
//        this.bodyBone = "ignore";
//        this.leftArmBone = "ignore";
//        this.rightArmBone = "ignore";
//        this.leftLegBone = "ignore";
//        this.rightLegBone = "ignore";
//        this.leftBootBone = "ignore";
//        this.rightBootBone = "ignore";
//    }

    public GogglesItemRenderer() {
        super(new GogglesItemModel());
        this.headBone = "goggles";
        this.bodyBone = "ignore";
        this.leftArmBone = "ignore";
        this.rightArmBone = "ignore";
        this.leftLegBone = "ignore";
        this.rightLegBone = "ignore";
        this.leftBootBone = "ignore";
        this.rightBootBone = "ignore";
    }
}

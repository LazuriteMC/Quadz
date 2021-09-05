package dev.lazurite.quadz.client.render.renderer;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.model.GogglesItemModel;
import dev.lazurite.quadz.common.item.GogglesItem;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.GeoArmorRendererFactory;

public class GogglesItemRenderer extends GeoArmorRenderer<GogglesItem> {
    public GogglesItemRenderer(GeoArmorRendererFactory.Context ctx) {
        super(new GogglesItemModel(), ctx, new EntityModelLayer(new Identifier(Quadz.MODID, "goggles"), "main"));
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

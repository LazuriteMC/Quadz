package dev.lazurite.quadz.client.render.renderer;

import dev.lazurite.quadz.client.render.model.GogglesItemModel;
import dev.lazurite.quadz.common.item.GogglesItem;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class GogglesItemRenderer extends GeoArmorRenderer<GogglesItem> {
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

package dev.lazurite.quadz.client.render.item;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class QuadcopterItemRenderer extends GeoItemRenderer<QuadcopterItem> {

    public QuadcopterItemRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(Quadz.MODID, "quadcopter")));
    }

}

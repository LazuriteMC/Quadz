package dev.lazurite.quadz.client.render.item;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.item.GogglesItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class GogglesItemRenderer extends GeoArmorRenderer<GogglesItem> {

    public GogglesItemRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(Quadz.MODID, "armor/goggles")));
    }

}

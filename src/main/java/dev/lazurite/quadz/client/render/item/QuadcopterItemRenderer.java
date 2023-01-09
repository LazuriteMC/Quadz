package dev.lazurite.quadz.client.render.item;

import dev.lazurite.quadz.common.item.QuadcopterItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class QuadcopterItemRenderer extends GeoItemRenderer<QuadcopterItem> {

    public QuadcopterItemRenderer() {
        super(new QuadcopterItemModel());
    }

    @Override
    public QuadcopterItemModel getGeoModel() {
        return (QuadcopterItemModel) this.model;
    }

}

package dev.lazurite.quadz.common.hooks;

import dev.lazurite.form.api.loader.TemplateLoader;
import dev.lazurite.form.impl.common.template.model.Template;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.util.Bindable;
import dev.lazurite.quadz.common.util.Search;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.toolbox.api.util.PlayerUtil;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class ServerEventHooks {

    public static void onServerTick(MinecraftServer server) {
        var range = server.getPlayerList().getViewDistance() * 16;
        var toActivate = new ArrayList<Quadcopter>();

        for (var player : PlayerUtil.all(server)) {
            var pos = player.getCamera().position();
            var level = player.getLevel();
            var quadcopters = Search.allInArea(level, pos, range);

            player.sync();

            if (player.getCamera() instanceof Quadcopter quadcopter && !player.equals(quadcopter.getRigidBody().getPriorityPlayer())) {
                quadcopter.getRigidBody().prioritize(player);
            }

            // Don't forgor the camera!
            if (player.getCamera() instanceof Quadcopter quadcopter) {
                quadcopters.add(quadcopter);
            }

            quadcopters.forEach(quadcopter -> quadcopter.getEntityData().set(Quadcopter.ACTIVE, true));
            Bindable.get(player.getMainHandItem())
                    .flatMap(bindable -> Search.byBindId(level, pos, bindable.getBindId(), range))
                    .ifPresent(toActivate::add);
        }

        toActivate.forEach(quadcopter -> quadcopter.getEntityData().set(Quadcopter.ACTIVE, true));
    }

    public static void onEntityTemplateChanged(Entity entity) {
        if (entity instanceof Quadcopter quadcopter) {
            quadcopter.getRigidBody().setCollisionShape(MinecraftShape.convex(entity.getBoundingBox()));

            TemplateLoader.getTemplateById(quadcopter.getTemplate()).ifPresent(template -> {
                quadcopter.getEntityData().set(Quadcopter.CAMERA_ANGLE, template.metadata().get("cameraAngle").getAsInt());
                quadcopter.getRigidBody().setMass(template.metadata().get("mass").getAsFloat());
                quadcopter.getRigidBody().setDragCoefficient(template.metadata().get("dragCoefficient").getAsFloat());
            });
        }
    }

    public static void onTemplateLoaded(Template template) {
        Quadz.CREATIVE_MODE_TAB.rebuildSearchTree();
    }

    public static void onRebuildCreativeTab(FabricItemGroupEntries content) {
        content.accept(new ItemStack(Quadz.GOGGLES_ITEM));
        content.accept(new ItemStack(Quadz.REMOTE_ITEM));

        TemplateLoader.getTemplateByModId(Quadz.MODID).forEach(
                template -> content.accept(TemplateLoader.getItemStackFor(template, Quadz.QUADCOPTER_ITEM))
        );

    }

}

package bluevista.fpvracingmod.server;

import com.bluevista.fpvracing.FPVRacingMod;
import com.bluevista.fpvracing.server.entities.DroneEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityRegistry {

    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = new DeferredRegister<>(ForgeRegistries.ENTITIES, FPVRacingMod.MODID);
    public static final RegistryObject<EntityType<DroneEntity>> DRONE = ENTITY_TYPES.register("drone", () ->
            EntityType.Builder.<DroneEntity>create(DroneEntity::new, EntityClassification.MISC)
            .setCustomClientFactory(DroneEntity::new)
            .size(EntityType.PIG.getWidth(), EntityType.PIG.getHeight())
            .build(new ResourceLocation(FPVRacingMod.MODID, "drone").toString())
        );
}
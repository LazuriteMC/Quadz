package bluevista.fpvracing.mixin;

import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.server.items.DroneSpawnerItem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SummonCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.vecmath.Vector3f;

@Mixin(SummonCommand.class)
public class SummonCommandMixin {
    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/entity/EntityType;loadEntityWithPassengers(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/World;Ljava/util/function/Function;)Lnet/minecraft/entity/Entity;"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void execute(ServerCommandSource source, Identifier entity, Vec3d pos, CompoundTag nbt, boolean initialize, CallbackInfoReturnable<Integer> info, CompoundTag tag, ServerWorld world, Entity entity2) throws CommandSyntaxException {
        if (entity2 instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity2;

            drone.setRigidBodyPos(new Vector3f((float) pos.x, (float) pos.y, (float) pos.z));
            drone.yaw = source.getPlayer().yaw;

            DroneSpawnerItem.prepSpawnedDrone(source.getPlayer(), drone);
        }
    }
}

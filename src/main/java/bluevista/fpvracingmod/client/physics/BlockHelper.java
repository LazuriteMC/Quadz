package bluevista.fpvracingmod.client.physics;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.Map;

public class BlockHelper {
    public static Map<BlockPos, BlockState> getBlockList(ClientWorld world, Box area) {
        Map<BlockPos, BlockState> map = new HashMap();
        for(int i = (int) Math.floor(area.minX); i < Math.floor(area.maxX); i++) {
            for(int j = (int) Math.floor(area.minY); j < Math.floor(area.maxY); j++) {
                for(int k = (int) Math.floor(area.minZ); k < Math.floor(area.maxZ); k++) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    BlockState blockState = world.getBlockState(blockPos);
                    map.put(blockPos, blockState);
                }
            }
        }

        return map;
    }
}

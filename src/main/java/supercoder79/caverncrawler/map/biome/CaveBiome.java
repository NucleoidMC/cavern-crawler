package supercoder79.caverncrawler.map.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ChunkRegion;

import java.util.Random;

public interface CaveBiome {
    Direction[] DIRECTIONS = Direction.values();
    void generate(ChunkRegion world, Random random, BlockPos pos);

    int getColor();
}

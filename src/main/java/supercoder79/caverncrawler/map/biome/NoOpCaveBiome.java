package supercoder79.caverncrawler.map.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;

import java.util.Random;

public class NoOpCaveBiome implements CaveBiome{
    @Override
    public void generate(ChunkRegion world, Random random, BlockPos pos) {

    }
}

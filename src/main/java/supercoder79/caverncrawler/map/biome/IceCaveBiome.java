package supercoder79.caverncrawler.map.biome;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.gen.ChunkRandom;

import java.util.Random;

public final class IceCaveBiome implements CaveBiome {
    private final DoublePerlinNoiseSampler noise;
    private final DoublePerlinNoiseSampler noiseA;
    private final DoublePerlinNoiseSampler noiseB;
    private final DoublePerlinNoiseSampler noiseC;
    private final DoublePerlinNoiseSampler noiseD;

    public IceCaveBiome(long seed) {
        ChunkRandom random = new ChunkRandom(seed);
        this.noise = DoublePerlinNoiseSampler.method_30846(random, -5, new DoubleArrayList(ImmutableList.of(1.0, 1.0, 1.0)));
        this.noiseA = DoublePerlinNoiseSampler.method_30846(random, -6, new DoubleArrayList(ImmutableList.of(1.0, 1.0)));
        this.noiseB = DoublePerlinNoiseSampler.method_30846(random, -6, new DoubleArrayList(ImmutableList.of(1.0, 1.0)));
        this.noiseC = DoublePerlinNoiseSampler.method_30846(random, -6, new DoubleArrayList(ImmutableList.of(1.0, 1.0)));
        this.noiseD = DoublePerlinNoiseSampler.method_30846(random, -6, new DoubleArrayList(ImmutableList.of(1.0, 1.0)));
    }

    @Override
    public void generate(ChunkRegion world, Random random, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction direction : DIRECTIONS) {
            mutable.set(pos);
            mutable.move(direction);

            if (random.nextInt((int) Math.max(1, 4 + this.noise.sample(pos.getX(), pos.getY(), pos.getZ()) * 3)) == 0 && world.getBlockState(mutable).isOf(Blocks.STONE)) {
                int snowChance = direction == Direction.DOWN ? 4 : 0;

                if (random.nextInt((int) Math.max(1, 2 + this.noiseA.sample(pos.getX(), pos.getY(), pos.getZ()) * 4)) == 0) {
                    world.setBlockState(mutable, Blocks.ICE.getDefaultState(), 3);
                    snowChance = 0;
                } else if (random.nextInt((int) Math.max(1, 2 + this.noiseB.sample(-pos.getX(), pos.getY(), pos.getZ()) * 4)) == 0) {
                    world.setBlockState(mutable, Blocks.PACKED_ICE.getDefaultState(), 3);
                    snowChance = 0;
                } else if (random.nextInt((int) Math.max(1, 2 + this.noiseC.sample(pos.getX(), pos.getY(), -pos.getZ()) * 4)) == 0) {
                    world.setBlockState(mutable, Blocks.BLUE_ICE.getDefaultState(), 3);
                } else if (random.nextInt((int) Math.max(1, 5 + this.noiseD.sample(-pos.getX(), pos.getY(), -pos.getZ()) * 4)) == 0) {
                    world.setBlockState(mutable, Blocks.SNOW_BLOCK.getDefaultState(), 3);
                }

                if (snowChance > 0 && random.nextInt(snowChance) == 0) {
                    world.setBlockState(mutable.up(), Blocks.SNOW.getDefaultState(), 3);
                }
            }
        }
    }

    @Override
    public int getColor() {
        return 0x2eb6ff;
    }
}

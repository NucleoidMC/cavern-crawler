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

public final class ShroomCaveBiome implements CaveBiome {
    private final DoublePerlinNoiseSampler noise;

    public ShroomCaveBiome(long seed) {
        this.noise = DoublePerlinNoiseSampler.method_30846(new ChunkRandom(seed), -5, new DoubleArrayList(ImmutableList.of(1.0, 1.0)));
    }

    @Override
    public void generate(ChunkRegion world, Random random, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction direction : DIRECTIONS) {
            mutable.set(pos);
            mutable.move(direction);

            if (random.nextInt((int) Math.max(1, 4 + this.noise.sample(pos.getX(), pos.getY(), pos.getZ()) * 3)) == 0 && world.getBlockState(mutable).isOf(Blocks.STONE)) {
                if (direction == Direction.UP && random.nextInt(20) == 0) {
                    int length = random.nextInt(random.nextInt(4) + 1) + 1;

                    mutable.move(Direction.DOWN);
                    for (int i = 0; i < length; i++) {
                        if (!world.getBlockState(mutable).isAir()) {
                            break;
                        }

                        world.setBlockState(mutable, Blocks.SHROOMLIGHT.getDefaultState(), 3);

                        mutable.move(Direction.DOWN);
                    }
                } else {
                    world.setBlockState(mutable, Blocks.DIRT.getDefaultState(), 3);
                }
            }
        }
    }

    @Override
    public int getColor() {
        return 0xd9b445;
    }
}

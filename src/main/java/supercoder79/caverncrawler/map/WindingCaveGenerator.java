package supercoder79.caverncrawler.map;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.ChunkRandom;

import java.util.Random;

public final class WindingCaveGenerator {
    private final DoublePerlinNoiseSampler tunnelNoiseA;
    private final DoublePerlinNoiseSampler tunnelNoiseB;

    private static DoublePerlinNoiseSampler create(ChunkRandom random, int startingOctave, double... octaves) {
        return DoublePerlinNoiseSampler.method_30846(random, startingOctave, new DoubleArrayList(octaves));
    }

    public WindingCaveGenerator(Random random) {
        this.tunnelNoiseA = create(new ChunkRandom(random.nextLong()), -7, 1.0, 1.0);
        this.tunnelNoiseB = create(new ChunkRandom(random.nextLong()), -7, 1.0, 1.0);
    }

    public boolean shouldCarve(int x, int y, int z) {
        double noiseA = this.tunnelNoiseA.sample(x, y, z);
        double noiseB = this.tunnelNoiseB.sample(x, y, z);

        return noiseA * noiseA + noiseB * noiseB < 0.005;
    }
}

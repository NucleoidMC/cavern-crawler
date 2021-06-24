package supercoder79.caverncrawler.map.biome;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.ChunkRandom;

import java.util.HashMap;
import java.util.Map;

public final class CaveBiomePicker {
    private final Map<Vec3d, CaveBiome> biomes = new HashMap<>();
    private final DoublePerlinNoiseSampler x;
    private final DoublePerlinNoiseSampler y;
    private final DoublePerlinNoiseSampler z;

    public CaveBiomePicker(long seed) {
        ChunkRandom random = new ChunkRandom(seed);
        this.x = DoublePerlinNoiseSampler.method_30846(random, -7, new DoubleArrayList(ImmutableList.of(1.0, 1.0)));
        this.y = DoublePerlinNoiseSampler.method_30846(random, -7, new DoubleArrayList(ImmutableList.of(1.0, 1.0)));
        this.z = DoublePerlinNoiseSampler.method_30846(random, -7, new DoubleArrayList(ImmutableList.of(1.0, 1.0)));
    }

    public void add(CaveBiome biome, Vec3d point) {
        this.biomes.put(point, biome);
    }

    public CaveBiome pick(BlockPos pos) {
        Vec3d noise = new Vec3d(this.x.sample(pos.getX(), pos.getY(), pos.getZ()), this.y.sample(pos.getX(), pos.getY(), pos.getZ()), this.z.sample(pos.getX(), pos.getY(), pos.getZ()));
        Vec3d min = null;
        double minDist = Double.POSITIVE_INFINITY;

        for (Vec3d vec3d : this.biomes.keySet()) {
            double dist = noise.squaredDistanceTo(vec3d);

            if (dist < minDist) {
                minDist = dist;
                min = vec3d;
            }
        }

        return this.biomes.get(min);
    }
}

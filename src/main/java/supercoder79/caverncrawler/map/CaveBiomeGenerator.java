package supercoder79.caverncrawler.map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkRegion;
import supercoder79.caverncrawler.map.biome.CaveBiomePicker;
import supercoder79.caverncrawler.map.biome.IceCaveBiome;
import supercoder79.caverncrawler.map.biome.NoOpCaveBiome;
import supercoder79.caverncrawler.map.biome.ShroomCaveBiome;

import java.util.List;
import java.util.Random;

public final class CaveBiomeGenerator {
    private final CaveBiomePicker picker;

    public CaveBiomeGenerator(long seed) {
        this.picker = new CaveBiomePicker(seed);

        this.picker.add(new NoOpCaveBiome(), new Vec3d(0.0, 0.0, 0.0));
        this.picker.add(new ShroomCaveBiome(seed), new Vec3d(0.1, -0.2, -0.02));
        this.picker.add(new IceCaveBiome(seed), new Vec3d(-0.1, 0.05, -0.05));
    }

    public void generate(ChunkRegion world, Random random, List<BlockPos> positions) {
        for (BlockPos pos : positions) {
            // TODO: optimize
            this.picker.pick(pos).generate(world, random, pos);
        }
    }

    public CaveBiomePicker getPicker() {
        return picker;
    }
}

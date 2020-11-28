package supercoder79.caverncrawler.map.carver;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.ConfiguredCarver;

public class HorizontalCarver extends Carver<ProbabilityConfig> {
	public static ConfiguredCarver<ProbabilityConfig> INSTANCE = new ConfiguredCarver<>(new HorizontalCarver(), new ProbabilityConfig(1.f / 15.f));

	public HorizontalCarver() {
		super(ProbabilityConfig.CODEC, 256);
	}

	@Override
	public boolean carve(Chunk chunk, Function<BlockPos, Biome> posToBiome, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet carvingMask, ProbabilityConfig carverConfig) {
		double x = (chunkX * 16 + random.nextInt(16));
		double y = random.nextInt(80) + 8;
		double z = (chunkZ * 16 + random.nextInt(16));
		float yaw = 8.0F + random.nextFloat() * 3.5F;
		float pitch = yaw * 0.4f * (random.nextFloat() + 0.65f);

		for (int i = 0; i < random.nextInt(8) + 14; i++) {
			this.carveRegion(chunk, posToBiome, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x + 1.0D, y, z, yaw, pitch, carvingMask);

			x += (random.nextDouble() - random.nextDouble()) * yaw;
			y += MathHelper.sin(pitch) * 0.25;
			z += (random.nextDouble() - random.nextDouble()) * yaw;

			yaw += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 0.5F;
			pitch += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 0.5F;
		}

		return true;
	}

	@Override
	public boolean shouldCarve(Random random, int chunkX, int chunkZ, ProbabilityConfig config) {
		return random.nextFloat() <= config.probability;
	}

	@Override
	protected boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y) {
		return scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ >= 0.5D;
	}
}

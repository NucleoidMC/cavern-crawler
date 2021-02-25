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

public class VerticalCarver extends Carver<ProbabilityConfig> {
	public static ConfiguredCarver<ProbabilityConfig> INSTANCE = new ConfiguredCarver<>(new VerticalCarver(), new ProbabilityConfig(1 / 7.f));
	public VerticalCarver() {
		super(ProbabilityConfig.CODEC, 256);
	}

	@Override
	public boolean carve(Chunk chunk, Function<BlockPos, Biome> posToBiome, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet carvingMask, ProbabilityConfig carverConfig) {
		double x = chunkX * 16 + random.nextInt(16);
		double y = random.nextInt(250) + 8;
		double z = chunkZ * 16 + random.nextInt(16);
		float yaw = 1.5f + 3.5F + random.nextFloat() * 2.0F; // Base val: 1.5
		float pitch = yaw * 0.75f;

//		int branchingStart =
		for (int i = 0; i < random.nextInt(8) + 8; i++) {
			this.carveRegion(chunk, posToBiome, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x + 1.0D, y, z, yaw, pitch, carvingMask);

			float pitchChange = MathHelper.cos(pitch) * 2;
			x += MathHelper.cos(yaw) * pitchChange;
			y -= Math.max(1.25, Math.abs(MathHelper.sin(pitch) * 3)); // Force caves downwards
			z += MathHelper.sin(yaw) * pitchChange;

			yaw += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 0.75F;
			pitch += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 0.75F;
		}

		return true;
	}

	@Override
	public boolean shouldCarve(Random random, int chunkX, int chunkZ, ProbabilityConfig config) {
		return random.nextFloat() <= config.probability;
	}

	@Override
	protected boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y) {
		return scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ >= 0.8D;
	}
}

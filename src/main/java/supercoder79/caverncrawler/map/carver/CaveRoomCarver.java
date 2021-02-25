package supercoder79.caverncrawler.map.carver;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.ConfiguredCarver;

public class CaveRoomCarver extends Carver<ProbabilityConfig> {
	public static ConfiguredCarver<ProbabilityConfig> INSTANCE = new ConfiguredCarver<>(new CaveRoomCarver(), new ProbabilityConfig(1.f / 12.f));

	public CaveRoomCarver() {
		super(ProbabilityConfig.CODEC, 256);
	}

	@Override
	public boolean carve(Chunk chunk, Function<BlockPos, Biome> posToBiome, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet carvingMask, ProbabilityConfig carverConfig) {
		double x = chunkX * 16 + random.nextInt(16);
		double y = random.nextInt(200) + 40;
		double z = chunkZ * 16 + random.nextInt(16);

		double yaw = random.nextDouble() * 8 + 12;
		double pitch = yaw * (0.5 + (random.nextDouble() * 0.2));

		this.carveRegion(chunk, posToBiome, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, yaw, pitch, carvingMask);

		return true;
	}

	@Override
	public boolean shouldCarve(Random random, int chunkX, int chunkZ, ProbabilityConfig config) {
		// This shouldn't generate at spawn
		if (chunkX == 0 && chunkZ == 0) {
			return false;
		}

		return random.nextFloat() <= config.probability;
	}

	@Override
	protected boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y) {
		return scaledRelativeY <= -0.7D || scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ >= 0.75D;
	}
}

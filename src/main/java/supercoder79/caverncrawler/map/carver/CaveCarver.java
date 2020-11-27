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

public class CaveCarver extends Carver<ProbabilityConfig> {
	public static ConfiguredCarver<ProbabilityConfig> INSTANCE = new ConfiguredCarver<>(new CaveCarver(), new ProbabilityConfig(0.1f));
	public CaveCarver() {
		super(ProbabilityConfig.CODEC, 256);
	}

	public boolean shouldCarve(Random random, int chunkX, int chunkZ, ProbabilityConfig probabilityConfig) {
		// Force cave at spawn
		if (chunkX == 0 && chunkZ == 0) {
			return true;
		}

		return random.nextFloat() <= probabilityConfig.probability;
	}

	public boolean carve(Chunk chunk, Function<BlockPos, Biome> function, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet bitSet, ProbabilityConfig probabilityConfig) {
		int branchFactor = (this.getBranchFactor() * 2 - 1) * 16;
		int caveCount = random.nextInt(random.nextInt(random.nextInt(this.getMaxCaveCount()) + 1) + 1);

		for(int i = 0; i < caveCount; ++i) {
			double x = chunkX * 16 + random.nextInt(16);
			double y = this.getCaveY(random);
			double z = chunkZ * 16 + random.nextInt(16);
			int tunnelCount = 1;

			for(int j = 0; j < tunnelCount; ++j) {
				float yaw = random.nextFloat() * 6.2831855F;
				float pitch = (random.nextFloat() - 0.5F) / 4.0F;
				float width = 1 + ((random.nextFloat() - 0.5f) * 0.2f);
				int maxBranches = branchFactor - random.nextInt(branchFactor / 4);
				this.carveTunnels(chunk, function, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, width, yaw, pitch, 0, maxBranches, this.getTunnelSystemHeightWidthRatio(), bitSet);
			}
		}

		return true;
	}

	protected int getMaxCaveCount() {
		return 7;
	}

	@Override
	public int getBranchFactor() {
		return 4;
	}

	protected double getTunnelSystemHeightWidthRatio() {
		return 1.0D;
	}

	protected int getCaveY(Random random) {
		return random.nextInt(random.nextInt(120) + 8);
	}

	protected void carveTunnels(Chunk chunk, Function<BlockPos, Biome> postToBiome, long seed, int seaLevel, int mainChunkX, int mainChunkZ, double x, double y, double z, float width, float yaw, float pitch, int branchStartIndex, int branchCount, double yawPitchRatio, BitSet carvingMask) {
		Random random = new Random(seed);
		int nextBranch = random.nextInt(branchCount / 2) + branchCount / 4;
		float yawScale = 0.0F;
		float pitchScale = 0.0F;

		for(int branch = branchStartIndex; branch < branchCount; ++branch) {
			double scaledYaw = 1.5D + (double)(MathHelper.sin(3.1415927F * (float) branch / (float)branchCount) * width);
			double scaledPitch = scaledYaw * yawPitchRatio;
			float delta = MathHelper.cos(pitch);
			x += MathHelper.cos(yaw) * delta;
			y += MathHelper.sin(pitch);
			z += MathHelper.sin(yaw) * delta;
			pitch *= 0.7F;
			pitch += pitchScale * 0.1F;
			yaw += yawScale * 0.1F;
			pitchScale *= 0.9F;
			yawScale *= 0.75F;
			pitchScale += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			yawScale += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			if (branch == nextBranch && width > 1.0F) {
				this.carveTunnels(chunk, postToBiome, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, random.nextFloat() * 0.65F + 0.5F, yaw - 1.5707964F, pitch / 3.0F, branch, branchCount, 1.0D, carvingMask);
				this.carveTunnels(chunk, postToBiome, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, random.nextFloat() * 0.65F + 0.5F, yaw + 1.5707964F, pitch / 3.0F, branch, branchCount, 1.0D, carvingMask);
				return;
			}

			if (random.nextInt(4) != 0) {
				if (!this.canCarveBranch(mainChunkX, mainChunkZ, x, z, branch, branchCount, width)) {
					return;
				}

				this.carveRegion(chunk, postToBiome, seed, seaLevel, mainChunkX, mainChunkZ, x, y, z, scaledYaw * 2, scaledPitch * 2, carvingMask);
			}
		}

	}

	protected boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y) {
		return scaledRelativeY <= -0.7D || scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ >= 1.0D;
	}
}


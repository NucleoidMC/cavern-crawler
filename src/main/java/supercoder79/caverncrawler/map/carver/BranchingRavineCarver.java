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

public class BranchingRavineCarver extends Carver<ProbabilityConfig> {
   public static ConfiguredCarver<ProbabilityConfig> INSTANCE = new ConfiguredCarver<>(new BranchingRavineCarver(), new ProbabilityConfig(1 / 40.f));

   private final float[] heightToHorizontalStretchFactor = new float[1024];

   public BranchingRavineCarver() {
      super(ProbabilityConfig.CODEC, 256);
   }

   public boolean shouldCarve(Random random, int chunkX, int chunkZ, ProbabilityConfig probabilityConfig) {
      return random.nextFloat() <= probabilityConfig.probability;
   }

   public boolean carve(Chunk chunk, Function<BlockPos, Biome> function, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet bitSet, ProbabilityConfig probabilityConfig) {
      int branchingFactor = (this.getBranchFactor() * 2 - 1) * 16;
      double x = chunkX * 16 + random.nextInt(16);
      double y = 8 + random.nextInt(100);
      double z = chunkZ * 16 + random.nextInt(16);
      float yaw = (float) (random.nextFloat() * 6.2831855F + (Math.PI / 3.0f));
      float pitch = (random.nextFloat() - 0.5F) * 2.0F / 8.0F;
      float width = (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
      int branchCount = branchingFactor - random.nextInt(branchingFactor / 4);

      float factor = 1.0F;

      for(int y1 = 0; y1 < 256; ++y1) {
         if (y1 == 0 || random.nextInt(3) == 0) {
            factor = 1.0F + random.nextFloat() * random.nextFloat();
         }

         this.heightToHorizontalStretchFactor[y1] = factor * factor;
      }

      this.carveRavine(chunk, function, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, width, yaw, pitch, 0, branchCount, 3.0D, bitSet);
      return true;
   }

   private void carveRavine(Chunk chunk, Function<BlockPos, Biome> posToBiome, long seed, int seaLevel, int mainChunkX, int mainChunkZ, double x, double y, double z, float width, float yaw, float pitch, int branchStartIndex, int branchCount, double yawPitchRatio, BitSet carvingMask) {
      Random random = new Random(seed);

      float yawScale = 0.0F;
      float pitchScale = 0.0F;

      int nextBranch = random.nextInt(branchCount / 2) + branchCount / 4;
      for(int branch = branchStartIndex; branch < branchCount; ++branch) {
         double scaledYaw = 1.5D + (double)(MathHelper.sin((float) branch * 3.1415927F / (float)branchCount) * width);
         double scale3dPitch = scaledYaw * yawPitchRatio;
         scaledYaw *= (double)random.nextFloat() * 0.25D + 0.75D;
         scale3dPitch *= (double)random.nextFloat() * 0.25D + 0.75D;
         float deltaXZ = MathHelper.cos(pitch);
         float deltaY = MathHelper.sin(pitch);
         x += MathHelper.cos(yaw) * deltaXZ;
         y += deltaY;
         z += MathHelper.sin(yaw) * deltaXZ;
         pitch *= 0.7F;
         pitch += pitchScale * 0.05F;
         yaw += yawScale * 0.05F;
         pitchScale *= 0.8F;
         yawScale *= 0.5F;
         pitchScale += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
         yawScale += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

         if (branch == nextBranch) {
            this.carveRavine(chunk, posToBiome, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, width - (random.nextFloat() * 0.85F + 0.25f), random.nextFloat() * 0.75f * 6.2831855F, pitch, branch, branchCount, yawPitchRatio, carvingMask);
         }

         if (random.nextInt(4) != 0) {
            if (!this.canCarveBranch(mainChunkX, mainChunkZ, x, z, branch, branchCount, width)) {
               return;
            }

            this.carveRegion(chunk, posToBiome, seed, seaLevel, mainChunkX, mainChunkZ, x, y, z, scaledYaw, scale3dPitch, carvingMask);
         }
      }

   }

   protected boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y) {
      return (scaledRelativeX * scaledRelativeX + scaledRelativeZ * scaledRelativeZ) * (double)this.heightToHorizontalStretchFactor[y - 1] + scaledRelativeY * scaledRelativeY / 6.0D >= 1.0D;
   }
}
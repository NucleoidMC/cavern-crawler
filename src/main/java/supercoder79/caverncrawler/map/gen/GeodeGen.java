package supercoder79.caverncrawler.map.gen;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import supercoder79.caverncrawler.game.config.GeodeConfig;
import xyz.nucleoid.plasmid.game.gen.MapGen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.ChunkRandom;

public class GeodeGen implements MapGen {
	public static final GeodeGen INSTANCE = new GeodeGen(new GeodeConfig(3, 3, 4, 0.05));

	private static final WeightedList<Block> LIST = new WeightedList<Block>()
			.add(Blocks.IRON_ORE, 4)
			.add(Blocks.GOLD_ORE, 2)
			.add(Blocks.LAPIS_ORE, 2)
			.add(Blocks.EMERALD_ORE, 1)
			.add(Blocks.DIAMOND_ORE, 1);

	private final GeodeConfig config;

	private GeodeGen(GeodeConfig config) {
		this.config = config;
	}

	public static GeodeGen of(GeodeConfig config) {
		return new GeodeGen(config);
	}


	@Override
	public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
		BlockState ore = LIST.pickRandom(random).getDefaultState();

		int minGenOffset = -24;
		int maxGenOffset = 24;

		// Return if there is water here, to not generate in oceans.
		if (world.getFluidState(pos.add(0, maxGenOffset / 3, 0)).isStill()) {
			return;
		} else {
			// A list of positions to generate the geode around
			List<Pair<BlockPos, Integer>> generationPoints = Lists.newLinkedList();

			// Generate distribution points
			int distributionPoints = this.config.baseSize + random.nextInt(this.config.randomSize);

			DoublePerlinNoiseSampler noiseSampler = DoublePerlinNoiseSampler.method_30846(new ChunkRandom(((ChunkRegion)world).getSeed()), -4, new DoubleArrayList(new double[]{1.0D}));

			List<BlockPos> crackPoints = Lists.newLinkedList();
			double d = (double) distributionPoints / (double)(this.config.baseSize + this.config.randomSize);

			// Create threshold values for each layer. When the distance is less than this value, the specified layer will be generated
			double fillingThreshold = 1.0D / Math.sqrt(1.7);
			double innerLayerThreshold = 1.0D / Math.sqrt(2.2 + d);
			double middleLayerThreshold = 1.0D / Math.sqrt(3.2 + d);
			double outerLayerThreshold = 1.0D / Math.sqrt(4.2 + d);

			// Create the threshold for the crack.
			double crackThreshold = 1.0D / Math.sqrt(2.0 + random.nextDouble() / 2.0D + (distributionPoints > 3 ? d : 0.0D));

			// Generate the crack if the random check succeeds.
			boolean generateCrack = (double)random.nextFloat() < 0.95;

			// Generate the points around which the geode will generate
			for(int points = 0; points < distributionPoints; ++points) {
				int xOffset = 4 + random.nextInt(8 - 4);
				int yOffset = 4 + random.nextInt(8 - 4);
				int zOffset = 4 + random.nextInt(8 - 4);
				generationPoints.add(Pair.of(pos.add(xOffset, yOffset, zOffset), 1 + random.nextInt(4 - 1)));
			}

			// Generate the crack points.
			// The crack can generate in 4 different ways, providing randomness but also providing control over how players will see the geode.
			if (generateCrack) {
				int crackType = random.nextInt(4);

				int crackDistance = distributionPoints * 2 + 1;
				if (crackType == 0) {
					crackPoints.add(pos.add(crackDistance, 7, 0));
					crackPoints.add(pos.add(crackDistance, 5, 0));
					crackPoints.add(pos.add(crackDistance, 1, 0));
				} else if (crackType == 1) {
					crackPoints.add(pos.add(0, 7, crackDistance));
					crackPoints.add(pos.add(0, 5, crackDistance));
					crackPoints.add(pos.add(0, 1, crackDistance));
				} else if (crackType == 2) {
					crackPoints.add(pos.add(crackDistance, 7, crackDistance));
					crackPoints.add(pos.add(crackDistance, 5, crackDistance));
					crackPoints.add(pos.add(crackDistance, 1, crackDistance));
				} else {
					crackPoints.add(pos.add(0, 7, 0));
					crackPoints.add(pos.add(0, 5, 0));
					crackPoints.add(pos.add(0, 1, 0));
				}
			}

			// A list of points to generate crystal buds around
			List<BlockPos> crystalPlacements = Lists.newArrayList();

			// Iterate the area to check each position for generation. By default, this checks a 32x32x32 region.
			Iterator<BlockPos> positions = BlockPos.iterate(pos.add(minGenOffset, minGenOffset, minGenOffset), pos.add(maxGenOffset, maxGenOffset, maxGenOffset)).iterator();

			while(true) {
				while(true) {
					double generationDistance;
					double crackDistance;
					BlockPos currentPos;
					do {
						// If we're out of positions, place the amethyst buds and return.
						if (!positions.hasNext()) {
							return;
						}

						// Move to the next pos
						currentPos = positions.next();

						// Get the noise value at this position, scaled by the noise multiplier
						double noiseVal = noiseSampler.sample(currentPos.getX(), currentPos.getY(), currentPos.getZ()) * 0.045;

						// Reset the values
						generationDistance = 0.0D;
						crackDistance = 0.0D;

						// Generate the threshold based on the distance between the current point to the generation points.
						// This allows the generator to create offsetted spheres, making it look less artifical.
						Pair<BlockPos, Integer> generationPoint;
						for(Iterator<Pair<BlockPos, Integer>> generationIterator = generationPoints.iterator(); generationIterator.hasNext(); generationDistance += MathHelper.fastInverseSqrt(currentPos.getSquaredDistance(generationPoint.getFirst()) + (double) generationPoint.getSecond()) + noiseVal) {
							generationPoint = generationIterator.next();
						}

						BlockPos crackPos;
						for(Iterator<BlockPos> crackIterator = crackPoints.iterator(); crackIterator.hasNext(); crackDistance += MathHelper.fastInverseSqrt(currentPos.getSquaredDistance(crackPos) + (double) 2.0) + noiseVal) {
							crackPos = crackIterator.next();
						}
					} while(generationDistance < outerLayerThreshold);

					// Generate air for cracks, if we're close to the crack threshold and far enough away from the center (filling threshold)
					if (generateCrack && crackDistance >= crackThreshold && generationDistance < fillingThreshold) {
						if (world.getFluidState(currentPos).isEmpty()) {
							world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 2);
						}
						// Generate the filling, air by default, if the value is greater than the filling threshold.
					} else if (generationDistance >= fillingThreshold) {
						world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 2);
					} else if (generationDistance >= innerLayerThreshold) {
						// Generate inner layer, or use alternate inner layer (ore block) if the chance check succeeds.

						boolean useAlternateInnerLayer = (double)random.nextFloat() < this.config.oreChance;
						if (useAlternateInnerLayer) {
							world.setBlockState(currentPos, ore, 2);
						} else {
							world.setBlockState(currentPos, Blocks.STONE.getDefaultState(), 2);
						}

						// Generate the middle layer
					} else if (generationDistance >= middleLayerThreshold) {
						world.setBlockState(currentPos, Blocks.STONE.getDefaultState(), 2);
						// Generate the outer layer
					} else if (generationDistance >= outerLayerThreshold) {
						world.setBlockState(currentPos, Blocks.STONE.getDefaultState(), 2);
					}
				}
			}
		}
	}
}

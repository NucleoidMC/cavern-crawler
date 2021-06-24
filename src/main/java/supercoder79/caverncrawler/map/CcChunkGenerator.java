package supercoder79.caverncrawler.map;

import java.util.*;

import supercoder79.caverncrawler.game.config.CcConfig;
import supercoder79.caverncrawler.game.config.OreConfig;
import supercoder79.caverncrawler.map.carver.*;
import supercoder79.caverncrawler.map.gen.GeodeGen;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.EmeraldOreFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

public class CcChunkGenerator extends GameChunkGenerator {
	private final long seed;
	private final List<ConfiguredCarver<ProbabilityConfig>> carvers = new ArrayList<>();
	private final CcConfig config;
	private final GeodeGen geodes;
	private final CaveGenerator caves;
	private final WindingCaveGenerator windingCaves;
	private final CaveBiomeGenerator biomes;

	public CcChunkGenerator(CcConfig config, MinecraftServer server) {
		super(createBiomeSource(server, BiomeKeys.PLAINS), new StructuresConfig(Optional.empty(), Collections.emptyMap()));
		this.config = config;

		this.geodes = GeodeGen.of(config.geode);
		this.caves = new CaveGenerator(new Random(), 0);
		this.windingCaves = new WindingCaveGenerator(new Random());

		this.seed = new Random().nextLong();
		this.carvers.add(CaveCarver.INSTANCE);
		this.carvers.add(CaveRoomCarver.INSTANCE);
		this.carvers.add(HorizontalCarver.INSTANCE);
		this.carvers.add(BranchingRavineCarver.INSTANCE);
		this.carvers.add(VerticalCarver.INSTANCE);

		this.biomes = new CaveBiomeGenerator(seed);
	}

	@Override
	public void populateNoise(WorldAccess world, StructureAccessor structures, Chunk chunk) {
		ChunkPos pos = chunk.getPos();
		boolean isSpawn = Math.abs(pos.x) == 0 && Math.abs(pos.z) == 0;
		int startX = pos.x * 16;
		int startZ = pos.z * 16;

		BlockPos.Mutable mutable = new BlockPos.Mutable();
		Random random = new Random();

		double[][][] noise = new double[5][5][33];

		for (int noiseX = 0; noiseX <= 4; noiseX++) {
			for (int noiseZ = 0; noiseZ <= 4; noiseZ++) {
				for (int noiseY = 0; noiseY <= 32; noiseY++) {
					double sample = this.caves.sample((noiseX + pos.x * 4) * 4, noiseY * 8, (noiseZ + pos.z * 4) * 4);

					if (noiseY > 28) {
						sample = MathHelper.lerp((33 - noiseY) / 4.0, sample, 10);
					}

					if (noiseY < 4) {
						sample = MathHelper.lerp((4 - noiseY) / 4.0, sample, 10);
					}

					noise[noiseX][noiseZ][noiseY] = sample;
				}
			}
		}

		for (int noiseX = 0; noiseX < 4; noiseX++) {
			for (int noiseZ = 0; noiseZ < 4; noiseZ++) {
				for (int noiseY = 0; noiseY < 32; noiseY++) {
					double x0z0y0 = noise[noiseX][noiseZ][noiseY];
					double x0z1y0 = noise[noiseX][noiseZ + 1][noiseY];
					double x1z0y0 = noise[noiseX + 1][noiseZ][noiseY];
					double x1z1y0 = noise[noiseX + 1][noiseZ + 1][noiseY];
					double x0z0y1 = noise[noiseX][noiseZ][noiseY + 1];
					double x0z1y1 = noise[noiseX][noiseZ + 1][noiseY + 1];
					double x1z0y1 = noise[noiseX + 1][noiseZ][noiseY + 1];
					double x1z1y1 = noise[noiseX + 1][noiseZ + 1][noiseY + 1];

					for (int pieceY = 0; pieceY < 8; pieceY++) {
						double progressY = pieceY / 8.0;

						double x0z0 = MathHelper.lerp(progressY, x0z0y0, x0z0y1);
						double x0z1 = MathHelper.lerp(progressY, x0z1y0, x0z1y1);
						double x1z0 = MathHelper.lerp(progressY, x1z0y0, x1z0y1);
						double x1z1 = MathHelper.lerp(progressY, x1z1y0, x1z1y1);

						for (int pieceX = 0; pieceX < 4; pieceX++) {
							double progressX = pieceX / 4.0;
							double z0 = MathHelper.lerp(progressX, x0z0, x1z0);
							double z1 = MathHelper.lerp(progressX, x0z1, x1z1);

							for (int pieceZ = 0; pieceZ < 4; pieceZ++) {
								double progressZ = pieceZ / 4.0;

								double density = MathHelper.lerp(progressZ, z0, z1);

								mutable.set(noiseX * 4 + pieceX, noiseY * 8 + pieceY, noiseZ * 4 + pieceZ);

								chunk.setBlockState(mutable, getBlockState(isSpawn, mutable.getX() + startX, mutable.getZ() + startZ, mutable.getY(), random, density), false);
							}
						}
					}
				}
			}
		}
	}

	private BlockState getBlockState(boolean isSpawn, int x, int z, int y, Random random, double density) {
		BlockState state;
		if (density > 0) {
			if (this.windingCaves.shouldCarve(x, y, z)) {
				state = Blocks.CAVE_AIR.getDefaultState();
			} else {
				state = Blocks.STONE.getDefaultState();
			}
		} else {
			state = Blocks.CAVE_AIR.getDefaultState();
		}

		if (state.isAir() && y < 11) {
			state = Blocks.LAVA.getDefaultState();
		}

		if (y == 0 || y == 255) {
			state = Blocks.BEDROCK.getDefaultState();
		}

		if (isSpawn && (y > 116 && y < 124)) {
			state = Blocks.AIR.getDefaultState();
		}

		// TODO: glowstone on sides of walls
		if (isSpawn && (y == 116 || y == 124)) {
			if (y == 116 && (Math.abs(x - 8) <= 3 && Math.abs(z - 8) <= 3)) {
				return Blocks.BEDROCK.getDefaultState();
			}

			if (random.nextInt(5) == 0) {
				state = Blocks.GLOWSTONE.getDefaultState();
			}
		}

		return state;
	}

	@Override
	public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver step) {
		BiomeAccess biomeAccess = access.withSource(this.populationSource);
		ChunkRandom chunkRandom = new ChunkRandom();
		ChunkPos chunkPos = chunk.getPos();
		int chunkX = chunkPos.x;
		int chunkZ = chunkPos.z;
		BitSet bitSet = ((ProtoChunk)chunk).getOrCreateCarvingMask(step);

		for(int localChunkX = chunkX - 8; localChunkX <= chunkX + 8; ++localChunkX) {
			for(int localChunkZ = chunkZ - 8; localChunkZ <= chunkZ + 8; ++localChunkZ) {
				for (int i = 0; i < this.carvers.size(); i++) {
					chunkRandom.setCarverSeed(this.seed + i, localChunkX, localChunkZ);

					ConfiguredCarver<ProbabilityConfig> carver = this.carvers.get(i);
					if (carver.shouldCarve(chunkRandom, localChunkX, localChunkZ)) {
						carver.carve(chunk, biomeAccess::getBiome, chunkRandom, this.getSeaLevel(), localChunkX, localChunkZ, chunkX, chunkZ, bitSet);
					}
				}
			}
		}

	}

	@Override
	public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
		int chunkX = region.getCenterChunkX();
		int chunkZ = region.getCenterChunkZ();

		Random random = new Random();

		BlockPos.Mutable checkMutable = new BlockPos.Mutable();
		List<BlockPos> positions = new ArrayList<>();
		for (int x1 = 0; x1 < 16; x1++) {
			for (int z1 = 0; z1 < 16; z1++) {
				for (int y1 = 0; y1 < 256; y1++) {
					checkMutable.set(x1 + chunkX * 16, y1, z1 + chunkZ * 16);

					if (region.getBlockState(checkMutable).isAir()) {
						positions.add(checkMutable.toImmutable());
					}
				}
			}
		}

		this.biomes.generate(region, random, positions);

		if (chunkX == 0 && chunkZ == 0) {
			BlockPos.Mutable mutable = new BlockPos.Mutable();
			for (int x = 0; x < 16; x++) {
				mutable.setX(chunkX * 16 + x);

				for (int z = 0; z < 16; z++) {
					mutable.setZ(chunkZ * 16 + z);

					for (int y = 108; y <= 116; y++) {
						mutable.setY(y);
						if (region.getBlockState(mutable).isAir()) {
							region.setBlockState(mutable, Blocks.STONE.getDefaultState(), 3);
						}
					}

					for (int y = 124; y <= 132; y++) {
						mutable.setY(y);
						if (region.getBlockState(mutable).isAir()) {
							region.setBlockState(mutable, Blocks.STONE.getDefaultState(), 3);
						}
					}
				}
			}
		} else {
			if (chunkX == 0) {
				if (Math.abs(chunkZ) <= this.config.tunnelLength) {
					BlockPos.Mutable mutable = new BlockPos.Mutable();
					for (int x = 7; x <= 8; x++) {
						mutable.setX(chunkX * 16 + x);

						for (int z = 0; z < 16; z++) {
							mutable.setZ(chunkZ * 16 + z);

							for (int y = 117; y <= 120; y++) {
								mutable.setY(y);
								BlockState state = Blocks.AIR.getDefaultState();

								if (y == 120 && region.getBlockState(mutable).isOpaque()) {
									state = Blocks.STONE.getDefaultState();

									if (random.nextInt(6) == 0) {
										state = Blocks.GLOWSTONE.getDefaultState();
									}
								}

								region.setBlockState(mutable, state, 3);
							}
						}
					}
				}
			}

			if (chunkZ == 0) {
				if (Math.abs(chunkX) <= this.config.tunnelLength) {
					BlockPos.Mutable mutable = new BlockPos.Mutable();
					for (int x = 0; x < 16; x++) {
						mutable.setX(chunkX * 16 + x);

						for (int z = 7; z <= 8; z++) {
							mutable.setZ(chunkZ * 16 + z);

							for (int y = 117; y <= 120; y++) {
								mutable.setY(y);
								BlockState state = Blocks.AIR.getDefaultState();

								if (y == 120 && region.getBlockState(mutable).isOpaque()) {
									state = Blocks.STONE.getDefaultState();

									if (random.nextInt(6) == 0) {
										state = Blocks.GLOWSTONE.getDefaultState();
									}
								}

								region.setBlockState(mutable, state, 3);
							}
						}
					}
				}
			}
		}

		if (Math.abs(chunkX) <= 1 && Math.abs(chunkZ) <= 1) {
			for (int x = -1; x <= 16; x++) {
				for (int z = -1; z <= 16; z++) {
					if (x == -1 || x == 16 || z == -1 || z == 16) {
						for (int y = 117; y <= 120; y++) {
							BlockPos local = new BlockPos(x, y, z);
							if (region.getBlockState(local).isAir()) {
								region.setBlockState(local, Blocks.BARRIER.getDefaultState(), 3);
							}
						}
					}
				}
			}
		}

		if (Math.abs(chunkX) >= 1 && Math.abs(chunkZ) >= 1 && random.nextInt(this.config.geode.chance) == 0) {
			int x = random.nextInt(16) + (chunkX * 16);
			int y = random.nextInt(220) + 20;
			int z = random.nextInt(16) + (chunkZ * 16);
			this.geodes.generate(region, new BlockPos(x, y, z), random);
		}

		for (OreConfig ore : this.config.ores) {
			if (!ore.emeraldGeneration) {
				generateOre(region, random, ore.count, ore.size, ore.state, chunkX, chunkZ);
			} else {
				generateEmeraldOre(region, random, ore.count, ore.size, ore.state, chunkX, chunkZ);
			}
		}
	}

	private void generateOre(ChunkRegion region, Random random, int count, int size, BlockState state, int chunkX, int chunkZ) {
		for (int i = 0; i < count; i++) {
			int x = random.nextInt(16) + (chunkX * 16);
			int y = random.nextInt(256);
			int z = random.nextInt(16) + (chunkZ * 16);
			Feature.ORE.generate(region, this, random, new BlockPos(x, y, z), new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, state, size));
		}
	}

	private void generateEmeraldOre(ChunkRegion region, Random random, int count, int size, BlockState state, int chunkX, int chunkZ) {
		for (int i = 0; i < count; i++) {
			int x = random.nextInt(16) + (chunkX * 16);
			int y = random.nextInt(256);
			int z = random.nextInt(16) + (chunkZ * 16);
			Feature.EMERALD_ORE.generate(region, this, random, new BlockPos(x, y, z), new EmeraldOreFeatureConfig(Blocks.STONE.getDefaultState(), state));
		}
	}
}

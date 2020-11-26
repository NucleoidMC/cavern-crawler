package supercoder79.caverncrawler.map;

import java.util.BitSet;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import supercoder79.caverncrawler.map.carver.CaveCarver;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

public class CcChunkGenerator extends GameChunkGenerator {
	private final long seed;
	public CcChunkGenerator(MinecraftServer server) {
		super(createBiomeSource(server, BiomeKeys.PLAINS), new StructuresConfig(Optional.empty(), Collections.emptyMap()));

		this.seed = new Random().nextLong();
	}

	@Override
	public void populateNoise(WorldAccess world, StructureAccessor structures, Chunk chunk) {
		ChunkPos pos = chunk.getPos();
		boolean isSpawn = Math.abs(pos.x) == 0 && Math.abs(pos.z) == 0;

		BlockPos.Mutable mutable = new BlockPos.Mutable();
		Random random = new Random();

		for (int x = 0; x < 16; x++) {
			mutable.setX(x);

		    for (int z = 0; z < 16; z++) {
				mutable.setZ(z);

				for (int y = 0; y < 128; y++) {
					mutable.setY(y);

					BlockState state = Blocks.STONE.getDefaultState();
					if (y == 0 || y == 127) {
						state = Blocks.BEDROCK.getDefaultState();
					}

					if (isSpawn && (y > 56 && y < 64)) {
						state = Blocks.AIR.getDefaultState();
					}

					// TODO: glowstone on sides of walls
					if (isSpawn && (y == 56 || y == 64)) {
						if (random.nextInt(5) == 0) {
							state = Blocks.GLOWSTONE.getDefaultState();
						}
					}

					chunk.setBlockState(mutable, state, false);
				}
		    }
		}
	}

	@Override
	public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
		BiomeAccess biomeAccess = access.withSource(this.populationSource);
		ChunkRandom chunkRandom = new ChunkRandom();
		ChunkPos chunkPos = chunk.getPos();
		int chunkX = chunkPos.x;
		int chunkZ = chunkPos.z;
		BitSet bitSet = ((ProtoChunk)chunk).getOrCreateCarvingMask(carver);

		for(int localChunkX = chunkX - 8; localChunkX <= chunkX + 8; ++localChunkX) {
			for(int localChunkZ = chunkZ - 8; localChunkZ <= chunkZ + 8; ++localChunkZ) {
				chunkRandom.setCarverSeed(this.seed, localChunkX, localChunkZ);

				if (CaveCarver.INSTANCE.shouldCarve(chunkRandom, localChunkX, localChunkZ)) {
					CaveCarver.INSTANCE.carve(chunk, biomeAccess::getBiome, chunkRandom, this.getSeaLevel(), localChunkX, localChunkZ, chunkX, chunkZ, bitSet);
				}
			}
		}

	}

	@Override
	public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
		int chunkX = region.getCenterChunkX();
		int chunkZ = region.getCenterChunkZ();

		Random random = new Random();

		if (chunkX == 0 && chunkZ == 0) {
			BlockPos.Mutable mutable = new BlockPos.Mutable();
			for (int x = 0; x < 16; x++) {
				mutable.setX(chunkX * 16 + x);

			    for (int z = 0; z < 16; z++) {
			    	mutable.setZ(chunkZ * 16 + z);

					for (int y = 48; y <= 56; y++) {
						mutable.setY(y);
						if (region.getBlockState(mutable).isAir()) {
							region.setBlockState(mutable, Blocks.STONE.getDefaultState(), 3);
						}
					}
			    }
			}
		}

		for (int i = 0; i < 16; i++) {
			int x = random.nextInt(16) + (chunkX * 16);
			int y = random.nextInt(116) + 8;
			int z = random.nextInt(16) + (chunkZ * 16);
			Feature.ORE.generate(region, this, random, new BlockPos(x, y, z), new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, Blocks.COAL_ORE.getDefaultState(), 17));
		}

		for (int i = 0; i < 12; i++) {
			int x = random.nextInt(16) + (chunkX * 16);
			int y = random.nextInt(116) + 8;
			int z = random.nextInt(16) + (chunkZ * 16);
			Feature.ORE.generate(region, this, random, new BlockPos(x, y, z), new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, Blocks.IRON_ORE.getDefaultState(), 9));
		}

		for (int i = 0; i < 3; i++) {
			int x = random.nextInt(16) + (chunkX * 16);
			int y = random.nextInt(116) + 8;
			int z = random.nextInt(16) + (chunkZ * 16);
			Feature.ORE.generate(region, this, random, new BlockPos(x, y, z), new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, Blocks.GOLD_ORE.getDefaultState(), 9));
		}

		for (int i = 0; i < 8; i++) {
			int x = random.nextInt(16) + (chunkX * 16);
			int y = random.nextInt(116) + 8;
			int z = random.nextInt(16) + (chunkZ * 16);
			Feature.ORE.generate(region, this, random, new BlockPos(x, y, z), new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, Blocks.REDSTONE_ORE.getDefaultState(), 8));
		}

		for (int i = 0; i < 4; i++) {
			int x = random.nextInt(16) + (chunkX * 16);
			int y = random.nextInt(116) + 8;
			int z = random.nextInt(16) + (chunkZ * 16);
			Feature.ORE.generate(region, this, random, new BlockPos(x, y, z), new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, Blocks.LAPIS_ORE.getDefaultState(), 7));
		}

		for (int i = 0; i < 1; i++) {
			int x = random.nextInt(16) + (chunkX * 16);
			int y = random.nextInt(116) + 8;
			int z = random.nextInt(16) + (chunkZ * 16);
			Feature.ORE.generate(region, this, random, new BlockPos(x, y, z), new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, Blocks.DIAMOND_ORE.getDefaultState(), 8));
		}
	}
}

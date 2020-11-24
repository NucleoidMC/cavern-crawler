package supercoder79.caverncrawler.map;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructuresConfig;

public class CcChunkGenerator extends GameChunkGenerator {
	public CcChunkGenerator(MinecraftServer server) {
		super(createBiomeSource(server, BiomeKeys.PLAINS), new StructuresConfig(Optional.empty(), Collections.emptyMap()));
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

	}
}

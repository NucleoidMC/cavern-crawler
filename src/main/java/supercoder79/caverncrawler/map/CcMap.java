package supercoder79.caverncrawler.map;

import supercoder79.caverncrawler.game.CcConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class CcMap {
	private final CcConfig config;

	public CcMap(CcConfig config) {
		this.config = config;
	}

	public ChunkGenerator chunkGenerator(MinecraftServer server) {
		return new CcChunkGenerator(server);
	}
}

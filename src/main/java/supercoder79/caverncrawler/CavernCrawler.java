package supercoder79.caverncrawler;

import net.fabricmc.loader.api.FabricLoader;
import supercoder79.caverncrawler.command.MapCaveBiomesCommand;
import supercoder79.caverncrawler.command.PointsCommand;
import supercoder79.caverncrawler.game.CcWaiting;
import supercoder79.caverncrawler.game.config.CcConfig;
import xyz.nucleoid.plasmid.game.GameType;

import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.rule.GameRule;

public class CavernCrawler implements ModInitializer {
	public static final GameRule NO_ICE_MELT = new GameRule();

	@Override
	public void onInitialize() {
		GameType.register(
				new Identifier("caverncrawler", "caverncrawler"),
				CcWaiting::open,
				CcConfig.CODEC
		);

		PointsCommand.init();

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			MapCaveBiomesCommand.init();
		}
	}
}

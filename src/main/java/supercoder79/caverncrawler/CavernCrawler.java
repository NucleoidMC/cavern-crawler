package supercoder79.caverncrawler;

import supercoder79.caverncrawler.command.PointsCommand;
import supercoder79.caverncrawler.game.CcConfig;
import supercoder79.caverncrawler.game.CcWaiting;
import xyz.nucleoid.plasmid.game.GameType;

import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public class CavernCrawler implements ModInitializer {
	@Override
	public void onInitialize() {
		GameType.register(
				new Identifier("caverncrawler", "caverncrawler"),
				CcWaiting::open,
				CcConfig.CODEC
		);

		PointsCommand.init();
	}
}

package supercoder79.caverncrawler.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public final class CcConfig {
	public static final Codec<CcConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig)
	).apply(instance, CcConfig::new));

	public final PlayerConfig playerConfig;

	public CcConfig(PlayerConfig playerConfig) {
		this.playerConfig = playerConfig;
	}
}
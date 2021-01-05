package supercoder79.caverncrawler.game.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public final class CcConfig {
	public static final Codec<CcConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			OreConfig.CODEC.listOf().fieldOf("ores").forGetter(config -> config.ores),
			GeodeConfig.CODEC.fieldOf("geode").forGetter(config -> config.geode),
			Codec.INT.fieldOf("tunnel_length").forGetter(config -> config.tunnelLength),
			PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig)
	).apply(instance, CcConfig::new));

	public final List<OreConfig> ores;
	public final GeodeConfig geode;
	public final int tunnelLength;
	public final PlayerConfig playerConfig;

	public CcConfig(List<OreConfig> ores, GeodeConfig geode, int tunnelLength, PlayerConfig playerConfig) {
		this.ores = ores;
		this.geode = geode;
		this.tunnelLength = tunnelLength;
		this.playerConfig = playerConfig;
	}
}
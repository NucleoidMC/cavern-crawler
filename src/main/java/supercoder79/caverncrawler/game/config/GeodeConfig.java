package supercoder79.caverncrawler.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GeodeConfig {
	public static final Codec<GeodeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("chance").forGetter(c -> c.chance), // TODO: limit this to 1+
			Codec.INT.fieldOf("base_size").forGetter(c -> c.baseSize),
			Codec.INT.fieldOf("random_size").forGetter(c -> c.randomSize),
			Codec.DOUBLE.fieldOf("ore_chance").forGetter(c -> c.oreChance)
	).apply(instance, GeodeConfig::new));

	public final int chance;
	public final int baseSize;
	public final int randomSize;
	public final double oreChance;

	public GeodeConfig(int chance, int baseSize, int randomSize, double oreChance) {
		this.chance = chance;
		this.baseSize = baseSize;
		this.randomSize = randomSize;
		this.oreChance = oreChance;
	}
}

package supercoder79.caverncrawler.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class OreConfig {
	public static final Codec<OreConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("name").forGetter(c -> c.name),
			Codec.INT.fieldOf("count").forGetter(c -> c.count),
			Codec.INT.fieldOf("size").forGetter(c -> c.size),
			Codec.BOOL.fieldOf("emerald_generation").orElse(false).forGetter(c -> c.emeraldGeneration)
	).apply(instance, OreConfig::new));
	private final Identifier name;

	public final BlockState state;
	public final int count;
	public final int size;
	public final boolean emeraldGeneration;

	public OreConfig(Identifier name, int count, int size, boolean emeraldGeneration) {
		this.state = Registry.BLOCK.get(name).getDefaultState();
		this.name = name;
		this.count = count;
		this.size = size;
		this.emeraldGeneration = emeraldGeneration;
	}
}

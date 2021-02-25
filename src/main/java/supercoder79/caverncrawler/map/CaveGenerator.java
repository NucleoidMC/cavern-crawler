package supercoder79.caverncrawler.map;

import java.util.Random;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.ChunkRandom;

public class CaveGenerator {
	private final int minY;
	private final DoublePerlinNoiseSampler verticalOffsetNoise;
	private final DoublePerlinNoiseSampler verticalAdditionNoise;
	private final DoublePerlinNoiseSampler falloffNoise;
	private final DoublePerlinNoiseSampler pow3AdditionNoise;
	private final DoublePerlinNoiseSampler scaledCaveScaleNoise;
	private final DoublePerlinNoiseSampler horizontalCaveNoise;
	private final DoublePerlinNoiseSampler caveScaleNoise;
	private final DoublePerlinNoiseSampler caveExtentNoise;
	private final DoublePerlinNoiseSampler tunnelNoise1;
	private final DoublePerlinNoiseSampler tunnelNoise2;
	private final DoublePerlinNoiseSampler tunnelScaleNoise;
	private final DoublePerlinNoiseSampler tunnelFalloffNoise;
	private final DoublePerlinNoiseSampler offsetNoise;
	private final DoublePerlinNoiseSampler offsetScaleNoise;

	private final DoublePerlinNoiseSampler terrainNoise;

	private static DoublePerlinNoiseSampler create(ChunkRandom random, int startingOctave, double... octaves) {
		return DoublePerlinNoiseSampler.method_30846(random, startingOctave, new DoubleArrayList(octaves));
	}

	

	public CaveGenerator(Random random, int minY) {
		this.minY = minY;
		this.verticalAdditionNoise = create(new ChunkRandom(random.nextLong()), -7, 1.0, 1.0);
		this.falloffNoise = create(new ChunkRandom(random.nextLong()), -8, 1.0);
		this.pow3AdditionNoise = create(new ChunkRandom(random.nextLong()), -8, 1.0);
		this.scaledCaveScaleNoise = create(new ChunkRandom(random.nextLong()), -7, 1.0);
		this.horizontalCaveNoise = create(new ChunkRandom(random.nextLong()), -8, 1.0);
		this.caveScaleNoise = create(new ChunkRandom(random.nextLong()), -11, 1.0);
		this.caveExtentNoise = create(new ChunkRandom(random.nextLong()), -11, 1.0);
		this.tunnelNoise1 = create(new ChunkRandom(random.nextLong()), -7, 1.0);
		this.tunnelNoise2 = create(new ChunkRandom(random.nextLong()), -7, 1.0);
		this.tunnelScaleNoise = create(new ChunkRandom(random.nextLong()), -11, 1.0);
		this.tunnelFalloffNoise = create(new ChunkRandom(random.nextLong()), -8, 1.0);
		this.offsetNoise = create(new ChunkRandom(random.nextLong()), -5, 1.0);
		this.offsetScaleNoise = create(new ChunkRandom(random.nextLong()), -8, 1.0);
		this.verticalOffsetNoise = create(new ChunkRandom(random.nextLong()), -8, 1.0);
		this.terrainNoise = create(new ChunkRandom(random.nextLong()), -8, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
	}

	public double sample(int x, int y, int z) {
		double offsetNoise = this.getOffsetNoise(x, y, z);
		double tunnelNoise = this.getTunnelNoise(x, y, z);

		double terrainNoise = this.terrainNoise.sample(x, y, z) * 100;

		double scaledNoise = terrainNoise / 128.0D;
		double clampedNoise = MathHelper.clamp(scaledNoise + 0.45D, -1.0D, 1.0D);
		double verticalNoise = this.getVerticalNoise(x, y, z);
		double caveNoise = this.getCaveNoise(x, y, z);
		double clampedVerticalNoise = clampedNoise + verticalNoise;
		double minNoise = Math.min(clampedVerticalNoise, Math.min(tunnelNoise, caveNoise) + offsetNoise);
		double finalNoise = Math.max(minNoise, this.getAdditionNoise(x, y, z));
		return 128.0D * MathHelper.clamp(finalNoise, -1.0D, 1.0D);
	}

	private double getAdditionNoise(int x, int y, int z) {
		double falloffNoise = lerpFromProgress(this.falloffNoise, x, y, z, 0.0D, 2.0D);
		double pow3Noise = lerpFromProgress(this.pow3AdditionNoise, x, y, z, 0.0D, 1.0D);
		pow3Noise = Math.pow(pow3Noise, 3.0D);
		double verticalNoise = this.verticalAdditionNoise.sample(x * 25.0D, y * 0.3D, z * 25.0D);
		verticalNoise = pow3Noise * (verticalNoise * 2.0D - falloffNoise);
		return verticalNoise > 0.02D ? verticalNoise : Double.NEGATIVE_INFINITY;
	}

	private double getVerticalNoise(int x, int y, int z) {
		double noise = this.verticalOffsetNoise.sample(x, (y * 8), z);
		return noise * noise * 4.0D;
	}

	private double getTunnelNoise(int x, int y, int z) {
		double tunnelScaleNoise = this.tunnelScaleNoise.sample((x * 2), y, (z * 2));
		double scale = scaleTunnels(tunnelScaleNoise);
		double falloff = lerpFromProgress(this.tunnelFalloffNoise, x, y, z, 0.065D, 0.088);
		double tunnel1 = sample(this.tunnelNoise1, x, y, z, scale);
		double scaledTunnel1 = Math.abs(scale * tunnel1) - falloff;
		double tunnel2 = sample(this.tunnelNoise2, x, y, z, scale);
		double scaledTunnel2 = Math.abs(scale * tunnel2) - falloff;
		return clamp(Math.max(scaledTunnel1, scaledTunnel2));
	}

	private double getCaveNoise(int x, int y, int z) {
		double caveScaleNoise = this.caveScaleNoise.sample((x * 2), y, (z * 2));
		double scale = scaleCaves(caveScaleNoise);
		double falloff = lerpFromProgress(this.caveExtentNoise, (x * 2), y, (z * 2), 0.6D, 1.3D);
		double caveScale = sample(this.scaledCaveScaleNoise, x, y, z, scale);
		double scaledScale = Math.abs(scale * caveScale) - 0.083D * falloff;
		double horizontalNoise = lerpFromProgress(this.horizontalCaveNoise, x, 0.0D, z, this.minY, 8.0D);
		double noise = Math.abs(horizontalNoise - y / 8.0D) - falloff;
		noise = noise * noise * noise;
		return clamp(Math.max(noise, scaledScale));
	}

	private double getOffsetNoise(int x, int y, int z) {
		double noise = lerpFromProgress(this.offsetScaleNoise, x, y, z, 0.0D, 0.1D);
		return (0.4D - Math.abs(this.offsetNoise.sample(x, y, z))) * noise;
	}

	private static double scaleCaves(double value) {
		if (value < -0.75D) {
			return 0.5D;
		} else if (value < -0.5D) {
			return 0.75D;
		} else if (value < 0.5D) {
			return 1.0D;
		} else {
			return value < 0.75D ? 2.0D : 3.0D;
		}
	}

	private static double scaleTunnels(double value) {
		if (value < -0.5D) {
			return 0.75D;
		} else if (value < 0.0D) {
			return 1.0D;
		} else {
			return value < 0.5D ? 1.5D : 2.0D;
		}
	}

	private static double clamp(double value) {
		return MathHelper.clamp(value, -1.0D, 1.0D);
	}

	private static double sample(DoublePerlinNoiseSampler sampler, double x, double y, double z, double scale) {
		return sampler.sample(x / scale, y / scale, z / scale);
	}

	private static double lerpFromProgress(DoublePerlinNoiseSampler sampler, double x, double y, double z, double start, double end) {
		double noise = sampler.sample(x, y, z);
		return lerpFromProgress(noise, -1.0D, 1.0D, start, end);
	}

	private static double lerpFromProgress(double lerpValue, double lerpStart, double lerpEnd, double start, double end) {
		return MathHelper.lerp(MathHelper.getLerpProgress(lerpValue, lerpStart, lerpEnd), start, end);
	}
}

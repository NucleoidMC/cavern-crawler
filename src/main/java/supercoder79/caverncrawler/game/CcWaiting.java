package supercoder79.caverncrawler.game;

import supercoder79.caverncrawler.map.CcMap;
import supercoder79.caverncrawler.map.CcMapGenerator;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.fantasy.BubbleWorldSpawner;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public final class CcWaiting {
	private final GameSpace world;
	private final CcMap map;
	private final CcConfig config;

	private CcWaiting(GameSpace world, CcMap map, CcConfig config) {
		this.world = world;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<CcConfig> context) {
		CcMapGenerator generator = new CcMapGenerator();
		CcConfig config = context.getConfig();

		CcMap map = generator.create(config);
		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
				.setGenerator(map.chunkGenerator(context.getServer()))
				.setDefaultGameMode(GameMode.SPECTATOR)
				.setSpawner(BubbleWorldSpawner.atSurface(0, 0))
				.setTimeOfDay(6000)
				.setDifficulty(Difficulty.NORMAL);

		return context.createOpenProcedure(worldConfig, (game) -> {
			CcWaiting waiting = new CcWaiting(game.getSpace(), map, context.getConfig());

			GameWaitingLobby.applyTo(game, context.getConfig().playerConfig);

			game.setRule(GameRule.CRAFTING, RuleResult.DENY);
			game.setRule(GameRule.PORTALS, RuleResult.DENY);
			game.setRule(GameRule.PVP, RuleResult.DENY);
			game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
			game.setRule(GameRule.HUNGER, RuleResult.DENY);
			game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);

			game.on(RequestStartListener.EVENT, waiting::requestStart);

			game.on(PlayerAddListener.EVENT, waiting::addPlayer);
			game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
		});
	}

	private StartResult requestStart() {
		CcActive.open(this.world, this.map, this.config);
		return StartResult.OK;
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.spawnPlayer(player);
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		this.spawnPlayer(player);
		return ActionResult.FAIL;
	}

	private void spawnPlayer(ServerPlayerEntity player) {
		resetPlayer(player, GameMode.ADVENTURE);

		ServerWorld world = this.world.getWorld();

		ChunkPos chunkPos = new ChunkPos(0, 0);
		world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());

		player.teleport(world, 8, 59, 8, 0.0F, 0.0F);
	}

	public static void resetPlayer(ServerPlayerEntity player, GameMode mode) {
		player.inventory.clear();
		player.getEnderChestInventory().clear();
		player.clearStatusEffects();
		player.setHealth(20.0F);
		player.getHungerManager().setFoodLevel(20);
		player.getHungerManager().add(5, 0.5F);
		player.fallDistance = 0.0F;
		player.setGameMode(mode);
		player.setExperienceLevel(0);
		player.setExperiencePoints(0);
	}
}

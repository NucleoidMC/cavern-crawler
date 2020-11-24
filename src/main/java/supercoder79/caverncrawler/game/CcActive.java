package supercoder79.caverncrawler.game;

import java.util.HashSet;
import java.util.Set;

import supercoder79.caverncrawler.map.CcMap;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.BreakBlockListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class CcActive {
	public final GameWorld world;
	public final CcMap map;
	public final CcConfig config;
	private final Set<ServerPlayerEntity> participants;

	private CcActive(GameWorld world, CcMap map, CcConfig config, Set<ServerPlayerEntity> participants) {
		this.world = world;
		this.map = map;
		this.config = config;
		this.participants = participants;
	}

	public static void open(GameWorld world, CcMap map, CcConfig config) {
		CcActive active = new CcActive(world, map, config, new HashSet<>(world.getPlayers()));

		world.openGame(game -> {
			game.setRule(GameRule.CRAFTING, RuleResult.DENY);
			game.setRule(GameRule.PORTALS, RuleResult.DENY);
			game.setRule(GameRule.PVP, RuleResult.DENY);
			game.setRule(GameRule.BLOCK_DROPS, RuleResult.ALLOW);
			game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
			game.setRule(GameRule.HUNGER, RuleResult.DENY);
			game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);

			game.on(GameOpenListener.EVENT, active::open);
			game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());

			game.on(BreakBlockListener.EVENT, active::onBreak);
			game.on(UseBlockListener.EVENT, active::onUseBlock);

			game.on(GameTickListener.EVENT, active::tick);
		});
	}



	private void open() {

	}

	private void tick() {
	}

	private ActionResult onUseBlock(ServerPlayerEntity playerEntity, Hand hand, BlockHitResult hitResult) {
		return ActionResult.PASS;
	}

	private ActionResult onBreak(ServerPlayerEntity playerEntity, BlockPos pos) {
		return ActionResult.PASS;
	}
}

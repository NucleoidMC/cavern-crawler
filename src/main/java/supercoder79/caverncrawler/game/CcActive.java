package supercoder79.caverncrawler.game;

import supercoder79.caverncrawler.map.CcMap;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.BreakBlockListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class CcActive {
	public final GameSpace space;
	public final CcMap map;
	public final CcConfig config;
	private final PlayerSet participants;

	private CcActive(GameSpace space, CcMap map, CcConfig config, PlayerSet participants) {
		this.space = space;
		this.map = map;
		this.config = config;
		this.participants = participants;
	}

	public static void open(GameSpace space, CcMap map, CcConfig config) {
		CcActive active = new CcActive(space, map, config, space.getPlayers());

		space.openGame(game -> {
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

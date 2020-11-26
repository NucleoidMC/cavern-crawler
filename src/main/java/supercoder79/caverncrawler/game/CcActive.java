package supercoder79.caverncrawler.game;

import java.util.HashMap;
import java.util.Map;

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
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class CcActive {
	private static final Map<Block, Integer> BLOCK_2_POINTS = new HashMap<>();
	public final GameSpace space;
	public final CcMap map;
	public final CcConfig config;
	public final Map<ServerPlayerEntity, Integer> pointMap = new HashMap<>();
	private final PlayerSet participants;
	private final CcScoreboard scoreboard;

	private CcActive(GameSpace space, CcMap map, CcConfig config, PlayerSet participants, GlobalWidgets widgets) {
		this.space = space;
		this.map = map;
		this.config = config;
		this.participants = participants;
		this.scoreboard = new CcScoreboard(widgets);
	}

	public static void open(GameSpace space, CcMap map, CcConfig config) {
		space.openGame(game -> {
			GlobalWidgets widgets = new GlobalWidgets(game);
			CcActive active = new CcActive(space, map, config, space.getPlayers(), widgets);

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
		for (ServerPlayerEntity player : this.participants) {
			CcWaiting.resetPlayer(player, GameMode.SURVIVAL);

			player.inventory.insertStack(0, ItemStackBuilder.of(Items.IRON_PICKAXE).setUnbreakable().build());
			player.inventory.insertStack(8, ItemStackBuilder.of(Items.TORCH).setCount(64).build());
		}

		this.scoreboard.update(this.pointMap);
	}

	private void tick() {
	}

	private ActionResult onUseBlock(ServerPlayerEntity playerEntity, Hand hand, BlockHitResult hitResult) {
		return ActionResult.PASS;
	}

	private ActionResult onBreak(ServerPlayerEntity player, BlockPos pos) {
		ServerWorld world = player.getServerWorld();
		BlockState state = world.getBlockState(pos);

		if (BLOCK_2_POINTS.containsKey(state.getBlock())) {
			int points = this.pointMap.getOrDefault(player, 0);
			points += BLOCK_2_POINTS.get(state.getBlock());

			this.pointMap.put(player, points);

			this.scoreboard.update(this.pointMap);
		}

		return ActionResult.PASS;
	}

	static {
		BLOCK_2_POINTS.put(Blocks.COAL_ORE, 1);
		BLOCK_2_POINTS.put(Blocks.IRON_ORE, 3);
		BLOCK_2_POINTS.put(Blocks.REDSTONE_ORE, 3);
		BLOCK_2_POINTS.put(Blocks.LAPIS_ORE, 3);
		BLOCK_2_POINTS.put(Blocks.GOLD_ORE, 5);
		BLOCK_2_POINTS.put(Blocks.EMERALD_ORE, 7);
		BLOCK_2_POINTS.put(Blocks.DIAMOND_ORE, 11);
	}
}

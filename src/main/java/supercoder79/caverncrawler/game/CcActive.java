package supercoder79.caverncrawler.game;

import java.util.HashMap;
import java.util.Map;

import supercoder79.caverncrawler.map.CcMap;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
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
	private final int endingTick;
	private final int closingTick;
	private int ticks = 0;
	private boolean closingText = false;

	private CcActive(GameSpace space, CcMap map, CcConfig config, PlayerSet participants, GlobalWidgets widgets) {
		this.space = space;
		this.map = map;
		this.config = config;
		this.participants = participants;
		this.scoreboard = new CcScoreboard(widgets);
		this.endingTick = 5 * 60 * 20; // 5 minutes
		this.closingTick = this.endingTick + (10 * 20);
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

			game.on(PlayerDamageListener.EVENT, active::onDamage);

			game.on(GameCloseListener.EVENT, active::onClose);

			game.on(GameTickListener.EVENT, active::tick);
		});
	}



	private void open() {
		for (ServerPlayerEntity player : this.participants) {
			CcWaiting.resetPlayer(player, GameMode.SURVIVAL);

			player.inventory.insertStack(0, ItemStackBuilder.of(Items.NETHERITE_PICKAXE)
					.addEnchantment(Enchantments.EFFICIENCY, 5)
					.setUnbreakable().build());
			player.inventory.offHand.set(0, ItemStackBuilder.of(Items.TORCH).setCount(64).build());

			player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 20 * 60 * 10, 1, false, false));
		}

		this.participants.sendMessage(new LiteralText("Welcome to Cavern Crawler! The goal of the game is to mine as many ores as possible."));
		this.participants.sendMessage(new LiteralText("The player with the most points at the end wins. Type /ccpoints to get a list of ores and the points that they give you."));
		this.participants.sendMessage(new LiteralText("All ores can spawn at any height, so just mine anywhere and you may find what you're looking for!"));

		this.scoreboard.update(this.endingTick - this.ticks, this.pointMap);
	}

	private void tick() {
		this.ticks++;

		if (this.ticks >= this.closingTick) {
			this.space.close(GameCloseReason.FINISHED);
		}

		if (this.ticks >= this.endingTick) {
			if (!this.closingText) {
				this.closingText = true;

				this.participants.forEach((player) -> CcWaiting.resetPlayer(player, GameMode.SPECTATOR));
				this.participants.forEach((player) -> participants.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 20 * 10, 0, false, false)));

				ServerPlayerEntity maxPlayer = null;
				int maxPoints = Integer.MIN_VALUE;

				for (ServerPlayerEntity participant : this.participants) {
					if (this.pointMap.containsKey(participant)) {
						int points = this.pointMap.get(participant);

						if (points > maxPoints) {
							maxPoints = points;
							maxPlayer = participant;
						}
					}
				}

				this.participants.sendMessage(new LiteralText(maxPlayer.getEntityName() + " won with " + maxPoints + " points!"));
			}
		} else {
			this.scoreboard.update(this.endingTick - this.ticks, this.pointMap);
		}
	}

	ActionResult onDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
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

			this.scoreboard.update(this.endingTick - this.ticks, this.pointMap);
		}

		// Prevent players from farming ores by placing them and breaking them
		if (state.isOf(Blocks.IRON_ORE) || state.isOf(Blocks.GOLD_ORE)) {
			world.breakBlock(pos, false);

			// I am in awe of my programming skills
			if (state.isOf(Blocks.IRON_ORE)) {
				world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.IRON_INGOT)));
			} else {
				world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.GOLD_INGOT)));
			}

			return ActionResult.FAIL;
		}

		// Drop torches along with coal
		if (state.isOf(Blocks.COAL_ORE)) {
			int count = world.random.nextInt(3);

			world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.TORCH, count)));

		}

		return ActionResult.PASS;
	}

	void onClose() {
		for (ServerPlayerEntity participant : this.participants) {
			participant.clearStatusEffects();
		}
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

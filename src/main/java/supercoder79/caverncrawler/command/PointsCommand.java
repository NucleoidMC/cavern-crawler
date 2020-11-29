package supercoder79.caverncrawler.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class PointsCommand {
	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("ccpoints");

			builder.executes(context -> execute(context.getSource()));

			dispatcher.register(builder);

		});
	}

	private static int execute(ServerCommandSource source) {
		source.sendFeedback(new LiteralText("Coal: 1 point"), false);
		source.sendFeedback(new LiteralText("Iron: 3 points"), false);
		source.sendFeedback(new LiteralText("Gold: 5 points"), false);
		source.sendFeedback(new LiteralText("Redstone: 3 points"), false);
		source.sendFeedback(new LiteralText("Lapis: 3 points"), false);
		source.sendFeedback(new LiteralText("Emerald: 7 points"), false);
		source.sendFeedback(new LiteralText("Diamond: 11 points"), false);

		return 0;
	}
}

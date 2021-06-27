package supercoder79.caverncrawler.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import supercoder79.caverncrawler.map.CcChunkGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class MapCaveBiomesCommand {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("mapcavebiomes")
                    .requires(source -> source.hasPermissionLevel(2));

            builder.executes(context -> execute(context.getSource()));

            dispatcher.register(builder);

        });
    }

    private static int execute(ServerCommandSource source) {
        BufferedImage img = new BufferedImage(4096, 4096, BufferedImage.TYPE_INT_RGB);

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        ChunkGenerator chunkGenerator = source.getWorld().getChunkManager().getChunkGenerator();
        if (!(chunkGenerator instanceof CcChunkGenerator)) {
            return 0;
        }

        CcChunkGenerator generator = (CcChunkGenerator) chunkGenerator;

        for (int x = -2048; x < 2048; x++) {
            if (x % 512 == 0) {
                source.sendFeedback(new LiteralText(((x + 2048) / 4096.0) * 100 + "%"), false);
            }

            for (int z = -2048; z < 2048; z++) {
                mutable.set(x, 0, z);

                img.setRGB(x + 2048, z + 2048, generator.getBiomes().getPicker().pick(mutable).getColor());
            }
        }

        // save the biome map
        Path p = Paths.get("cave_biome_map.png");
        try {
            ImageIO.write(img, "png", p.toAbsolutePath().toFile());
            source.sendFeedback(new LiteralText("Mapped cave biome colors!"), false);
        } catch (IOException e) {
            source.sendFeedback(new LiteralText("Something went wrong, check the log!"), true);
            e.printStackTrace();
        }

        return 0;
    }
}

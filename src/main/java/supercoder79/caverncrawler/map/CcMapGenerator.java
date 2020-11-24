package supercoder79.caverncrawler.map;

import java.util.concurrent.CompletableFuture;

import supercoder79.caverncrawler.game.CcConfig;

import net.minecraft.util.Util;

public final class CcMapGenerator {
    public CompletableFuture<CcMap> create(CcConfig config) {
        return CompletableFuture.supplyAsync(() -> new CcMap(config), Util.getMainWorkerExecutor());
    }
}
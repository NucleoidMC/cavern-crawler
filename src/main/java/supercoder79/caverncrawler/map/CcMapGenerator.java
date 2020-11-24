package supercoder79.caverncrawler.map;

import java.util.concurrent.CompletableFuture;

import supercoder79.caverncrawler.game.CcConfig;

import net.minecraft.util.Util;

public final class CcMapGenerator {
    public CcMap create(CcConfig config) {
        return new CcMap(config);
    }
}
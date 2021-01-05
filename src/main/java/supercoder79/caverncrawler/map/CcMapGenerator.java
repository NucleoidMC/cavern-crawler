package supercoder79.caverncrawler.map;

import supercoder79.caverncrawler.game.config.CcConfig;

public final class CcMapGenerator {
    public CcMap create(CcConfig config) {
        return new CcMap(config);
    }
}
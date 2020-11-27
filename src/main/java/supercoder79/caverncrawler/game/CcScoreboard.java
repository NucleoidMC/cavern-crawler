package supercoder79.caverncrawler.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;
import xyz.nucleoid.plasmid.widget.SidebarWidget;

import java.util.Map;

public class CcScoreboard {
    private final SidebarWidget sidebar;

    public CcScoreboard(GlobalWidgets widgets) {
        Text title = new LiteralText("Cavern Crawler").formatted(Formatting.GOLD, Formatting.BOLD);
        this.sidebar = widgets.addSidebar(title);
    }

    public void update(int ticksRemaining, Map<ServerPlayerEntity, Integer> points) {
        this.sidebar.set(content -> {
            int minutesRemaining = ticksRemaining / (20 * 60);
            int secondsRemaining = (ticksRemaining / 20) - (minutesRemaining * 60);
            content.writeLine(minutesRemaining + ":" + secondsRemaining + " remaining");

            for (Map.Entry<ServerPlayerEntity, Integer> entry : points.entrySet()) {
                String line = String.format(
                        "%s%s:%s %d point%s",
                        Formatting.AQUA,
                        entry.getKey().getEntityName(),
                        Formatting.RESET,
                        entry.getValue(),
                        entry.getValue() == 1 ? "" : "s"
                );
                content.writeLine(line.substring(0, 16));
            }
        });
    }
}
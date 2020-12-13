package supercoder79.caverncrawler.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;
import xyz.nucleoid.plasmid.widget.SidebarWidget;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

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
            String formattedSeconds = secondsRemaining < 10 ? "0" + secondsRemaining : String.valueOf(secondsRemaining);

            content.writeLine(minutesRemaining + ":" + formattedSeconds + " remaining");

            int count = 0;
            for (Map.Entry<ServerPlayerEntity, Integer> entry : points.entrySet()) {
                if (count == 15) {
                    break;
                }

                String line = String.format(
                        "%s: %d",
                        entry.getKey().getEntityName(),
                        entry.getValue()
                );
                content.writeLine(line);

                count++;
            }
        });
    }
}
package codes.biscuit.skyblockaddons.features.tablist;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class RenderColumn {

    @Getter private List<TabLine> lines = new LinkedList<>();

    public int size() {
        return lines.size();
    }

    public void addLine(TabLine line) {
        this.lines.add(line);
    }

    public int getMaxWidth() {
        int maxWidth = 0;

        for (TabLine tabLine : lines) {
            maxWidth = Math.max(maxWidth, tabLine.getWidth());
        }

        return maxWidth;
    }
}

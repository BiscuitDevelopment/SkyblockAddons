package codes.biscuit.skyblockaddons.features.tablist;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class ParsedTabSection {

    private ParsedTabColumn column;

    private List<String> lines = new LinkedList<>();

    public ParsedTabSection(ParsedTabColumn column) {
        this.column = column;
    }

    public void addLine(String line) {
        this.lines.add(line);
    }

    public int size() {
        return lines.size();
    }
}

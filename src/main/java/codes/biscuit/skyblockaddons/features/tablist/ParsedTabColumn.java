package codes.biscuit.skyblockaddons.features.tablist;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class ParsedTabColumn {

    private String title;
    private List<String> lines = new LinkedList<>();
    @Setter private List<ParsedTabSection> sections = new LinkedList<>();

    public ParsedTabColumn(String title) {
        this.title = title;
    }

    public void addLine(String line) {
        this.lines.add(line);
    }

    public void addSection(ParsedTabSection section) {
        this.sections.add(section);
    }

    public int size() {
        return lines.size() + 1;
    }
}

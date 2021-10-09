package codes.biscuit.skyblockaddons.newgui.chronomatron;

import codes.biscuit.skyblockaddons.newgui.GuiBase;
import codes.biscuit.skyblockaddons.newgui.elements.ContainerElement;

public class ChronomatronGui extends GuiBase {

    @Override
    protected void init() {
        add(new ContainerElement().fillToScreenWithMargin(0.1F)
//                .add(new TextElement().rel)
//                .add(new ContainerElement().rela)
        );
    }

    @Override
    protected void render() {
        super.render();
    }
}

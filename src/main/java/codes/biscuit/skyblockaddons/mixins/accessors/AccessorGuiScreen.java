package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(GuiScreen.class)
public interface AccessorGuiScreen {

    @Invoker("drawHoveringText")
    void drawHoveringText(List<String> text, int x, int y);
}

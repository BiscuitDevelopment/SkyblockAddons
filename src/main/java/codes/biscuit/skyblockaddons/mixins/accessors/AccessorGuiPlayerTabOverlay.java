package codes.biscuit.skyblockaddons.mixins.accessors;

import com.google.common.collect.Ordering;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiPlayerTabOverlay.class)
public interface AccessorGuiPlayerTabOverlay {

    @Accessor
    IChatComponent getHeader();

    @Accessor
    IChatComponent getFooter();

    @Accessor("field_175252_a")
    Ordering<NetworkPlayerInfo> getObfField();
}

package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderItem.class)
public interface AccessorRenderItem {

    @Invoker("renderModel")
    void renderModel(IBakedModel model, int color);
}

package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RenderGlobal.class)
public interface AccessorRenderGlobal {

    @Accessor
    Map<Integer, DestroyBlockProgress> getDamagedBlocks();

    @Accessor
    Framebuffer getEntityOutlineFramebuffer();

    @Accessor
    ShaderGroup getEntityOutlineShader();

}

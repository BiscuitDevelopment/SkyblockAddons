package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.shader.ShaderManager;
import net.minecraft.client.renderer.Tessellator;

public class WorldVertexBufferUploaderHook {

    public static boolean onRenderWorldRendererBuffer() {
//        if (true) return false;
        boolean canceled = ShaderManager.INSTANCE.onRenderWorldRendererBuffer();
        if (canceled) {
            Tessellator.getInstance().getWorldRenderer().reset();
        }
        return canceled;
    }
}

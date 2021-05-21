package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.features.EntityOutlines.EntityOutlineRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

public class RenderGlobalHook {

    private static final boolean stopLookingForOptifine = false;

    private static final Method isFastRender = null;
    private static final Method isShaders = null;
    private static final Method isAntialiasing = null;

    private static final Logger logger = SkyblockAddons.getLogger();

    public static boolean shouldRenderSkyblockItemOutlines() {
        return EntityOutlineRenderer.shouldRenderEntityOutlines();
    }

    public static void afterFramebufferDraw() {
        GlStateManager.enableDepth();
    }

    public static boolean blockRenderingSkyblockItemOutlines(ICamera camera, float partialTicks, double x, double y, double z) {
        return EntityOutlineRenderer.renderEntityOutlines(camera, partialTicks, x, y, z);
    }

    public static void onAddBlockBreakParticle(int breakerId, BlockPos pos, int progress) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        // On public islands, hypixel sends a progress = 10 update once it registers the start of block breaking
        if (breakerId == 0 && main.getUtils().getLocation() != Location.ISLAND &&
                pos.equals(MinecraftHook.prevClickBlock) && progress == 10) {
            //System.out.println(progress);
            MinecraftHook.startMineTime = System.currentTimeMillis();
        }

    }
}

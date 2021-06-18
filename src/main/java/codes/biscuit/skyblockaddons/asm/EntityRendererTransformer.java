package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.InjectionHelper;
import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerField;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class EntityRendererTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.renderer.EntityRenderer}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.EntityRenderer.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (InjectionHelper.matches(methodNode, TransformerMethod.getNightVisionBrightness)) {

                InjectionHelper.start()
                        .matchMethodHead()

                        .startCode()
                        // ReturnValue returnValue = new ReturnValue();
                        .newInstance("codes/biscuit/skyblockaddons/asm/utils/ReturnValue")
                        .storeAuto(0) // TODO Reference local variable by name maybe? "returnValue"?

                        // EntityRendererHook.onGetNightVisionBrightness(returnValue);
                            .loadAuto(0)
                            .callStaticMethod("codes/biscuit/skyblockaddons/asm/hooks/EntityRendererHook", "onGetNightVisionBrightness",
                                    "(Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V")

                            // if (returnValue.isCancelled())
                            .loadAuto(0)
                            .invokeInstanceMethod("codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled", "()Z")
                            .startIfEqual()
                                // return 1.0F;
                                .constantValue(1.0F)
                                .reeturn()
                            // }
                            .endIf()
                            .endCode()
                        .finish();

            } else if (InjectionHelper.matches(methodNode, TransformerMethod.updateCameraAndRender)) {

                InjectionHelper.start()
                        // Match at: if (this.mc.currentScreen != null)
                        .matchingOwner(TransformerClass.Minecraft).matchingField(TransformerField.currentScreen).endCondition()
                        // Inject before the if statement (2 instructions above)
                        .setInjectionOffset(-2)
                        // 6 lines backwards should be: this.renderEndNanoTime = System.nanoTime();
                        .addAnchorCondition(-6).matchingOwner(TransformerClass.EntityRenderer).matchingField(TransformerField.renderEndNanoTime).endCondition()

                        .injectCodeBefore()
                            // EntityRendererHook.onRenderScreenPre();
                            .callStaticMethod("codes/biscuit/skyblockaddons/asm/hooks/EntityRendererHook", "onRenderScreenPre", "()V")
                            .endCode()
                        .finish();
            }
        }
    }
}

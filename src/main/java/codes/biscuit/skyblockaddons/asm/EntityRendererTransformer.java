package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.*;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

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
            if (InjectionHelper.matches(methodNode, TransformerMethod.getMouseOver)) {

                InjectionHelper.start()
                        .matchingOpcode(Opcodes.DLOAD).matchingLocalVarNumber(5).endCondition()

                        .injectCodeBefore()
                            .load(InstructionBuilder.VariableType.OBJECT, 14) // list
                            // EntityRendererHook.removeEntities(list);
                            .callStaticMethod("codes/biscuit/skyblockaddons/asm/hooks/EntityRendererHook", "removeEntities", "(Ljava/util/List;)V")
                            .endCode()
                        .finish();

            } else if (InjectionHelper.matches(methodNode, TransformerMethod.getNightVisionBrightness)) {

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
                                .rеturn()
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

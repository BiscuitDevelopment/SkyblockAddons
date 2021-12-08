package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class EffectRendererTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.particle.EffectRenderer}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.EffectRenderer.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.addEffect.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertOnAddParticle());
            }

            /*
             * Insert {@link codes.biscuit.skyblockaddons.asm.hooks.EffectRendererHook#renderParticleOverlays(float)} right before the last call to depthMask(true).
             */
            else if (TransformerMethod.renderParticles.matches(methodNode)) {
                AbstractInsnNode last_depthFunc = null;

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();

                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESTATIC &&
                            ((MethodInsnNode) abstractNode).owner.equals(TransformerClass.GlStateManager.getNameRaw()) &&
                            ((MethodInsnNode) abstractNode).name.equals(TransformerMethod.depthMask.getName())) {
                        last_depthFunc = abstractNode;
                    }
                    else if (last_depthFunc != null &&
                            abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(last_depthFunc.getPrevious(), insertAfterRenderParticles());
                    }
                }
            }
        }
    }

    private InsnList insertOnAddParticle() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // effect

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, // EffectRendererHook.onAddParticle(effect);
                "codes/biscuit/skyblockaddons/asm/hooks/EffectRendererHook", "onAddParticle", "("+TransformerClass.EntityFX.getName()+")V", false));
        return list;
    }

    private InsnList insertAfterRenderParticles() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.FLOAD, 2)); // partialTicks
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, // EffectRendererHook.renderParticleOverlays(partialTicks);
                "codes/biscuit/skyblockaddons/asm/hooks/EffectRendererHook", "renderParticleOverlays", "(F)V", false));

        return list;
    }

}
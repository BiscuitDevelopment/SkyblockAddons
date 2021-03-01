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
            else if (TransformerMethod.renderParticles.matches(methodNode)) {
                AbstractInsnNode last_alphaFunc = null;

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();

                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESTATIC &&
                            ((MethodInsnNode) abstractNode).owner.equals(TransformerClass.GlStateManager.getNameRaw()) &&
                            ((MethodInsnNode) abstractNode).name.equals(TransformerMethod.alphaFunc.getName())) {
                        last_alphaFunc = abstractNode;
                    }
                    else if (last_alphaFunc != null &&
                            abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, insertAfterRenderParticles());
                        System.out.println("Found 2!");
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
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, // EffectRendererHook.renderParticleOutlines(partialTicks);
                "codes/biscuit/skyblockaddons/asm/hooks/EffectRendererHook", "renderParticleOutlines", "(F)V", false));

        return list;
    }

}
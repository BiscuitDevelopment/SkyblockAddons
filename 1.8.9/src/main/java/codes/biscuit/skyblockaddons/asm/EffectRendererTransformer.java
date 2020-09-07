package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

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
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertAddMenuButtons());
            }
        }
    }

    private InsnList insertAddMenuButtons() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // effect

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, // EffectRendererHook.onAddParticle(effect);
                "codes/biscuit/skyblockaddons/asm/hooks/EffectRendererHook", "onAddParticle", "("+TransformerClass.EntityFX.getName()+")V", false));
        return list;
    }
}
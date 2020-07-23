package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

public class RendererLivingEntityTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.renderer.entity.RendererLivingEntity}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.RendererLivingEntity.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.rotateCorpse.matches(methodNode)) {

                // Objective:
                // Find: s.equals("Dinnerbone");
                // Replace RendererLivingEntityHook.equals(s, "Dinnerbone");

                // Objective 2:
                // Find: ((EntityPlayer)bat).isWearing(EnumPlayerModelParts.CAPE);
                // Replace RendererLivingEntityHook.isWearing(((EntityPlayer)bat), EnumPlayerModelParts.CAPE;

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractNode;
                        if (methodInsnNode.owner.equals("java/lang/String") && methodInsnNode.name.equals("equals")) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/RendererLivingEntityHook",
                                    "equals", "(Ljava/lang/String;Ljava/lang/Object;)Z", false)); // RendererLivingEntityHook.equals(s, "Dinnerbone");

                            iterator.remove(); // Remove the old line.

                        } else if (methodInsnNode.owner.equals(TransformerClass.EntityPlayer.getNameRaw()) && TransformerMethod.isWearing.matches(methodInsnNode)) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/RendererLivingEntityHook",
                                    "isWearing", "("+TransformerClass.EntityPlayer.getName()+TransformerClass.EnumPlayerModelParts.getName()+")Z", false)); // RendererLivingEntityHook.equals(s, "Dinnerbone");

                            iterator.remove(); // Remove the old line.
                            break;
                        }
                    }
                }
            }
        }
    }
}

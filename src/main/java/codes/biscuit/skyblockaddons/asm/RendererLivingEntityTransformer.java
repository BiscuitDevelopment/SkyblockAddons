package codes.biscuit.skyblockaddons.asm;

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
        return new String[]{"net.minecraft.client.renderer.entity.RendererLivingEntity"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode);
            if (nameMatches(methodName, "rotateCorpse", "func_77043_a")) {

                // Objective:
                // Find: s.equals("Dinnerbone");
                // Replace RendererLivingEntityHook.equals(s, "Dinnerbone");

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractNode;
                        if (methodInsnNode.owner.equals("java/lang/String") && methodInsnNode.name.equals("equals")) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/RendererLivingEntityHook",
                                    "equals", "(Ljava/lang/String;Ljava/lang/Object;)Z", false)); // RendererLivingEntityHook.equals(s, "Dinnerbone");

                            iterator.remove(); // Remove the old line.
                            break;
                        }
                    }
                }

                // Objective:
                // Find: ((EntityPlayer)bat).isWearing(EnumPlayerModelParts.CAPE);
                // Replace RendererLivingEntityHook.isWearing(((EntityPlayer)bat), EnumPlayerModelParts.CAPE;

                iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractNode;
                        if (methodInsnNode.owner.equals("net/minecraft/entity/player/EntityPlayer") && methodInsnNode.name.equals("isWearing")) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/RendererLivingEntityHook",
                                    "isWearing", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z", false)); // RendererLivingEntityHook.equals(s, "Dinnerbone");

                            iterator.remove(); // Remove the old line.
                            break;
                        }
                    }
                }
            }
        }
    }
}

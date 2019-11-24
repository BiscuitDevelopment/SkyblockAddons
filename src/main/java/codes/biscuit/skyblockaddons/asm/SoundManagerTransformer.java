package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.tweaker.transformer.Transformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;
import java.util.List;

public class SoundManagerTransformer implements Transformer {

    /**
     * {@link net.minecraft.client.audio.SoundManager}
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.audio.SoundManager"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : (List<MethodNode>)classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode); // Map all of the method names.
            if (nameMatches(methodName,"playSound", "func_148611_c")) {

                // Objective:
                // Find: this.getNormalizedVolume(p_sound, soundpoolentry, soundcategory);
                // Replace method with: SoundManagerHook.getNormalizedVolume(this, p_sound, soundpoolentry, soundcategory);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (methodInsnNode.owner.equals("net/minecraft/client/audio/SoundManager") && nameMatches(methodInsnNode.name, "getNormalizedVolume", "func_148594_a", "a")) {

                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/SoundManagerHook",
                                    "getNormalizedVolume", "(Lnet/minecraft/client/audio/SoundManager;Lnet/minecraft/client/audio/ISound;Lnet/minecraft/client/audio/SoundPoolEntry;Lnet/minecraft/client/audio/SoundCategory;)F",
                                    false)); // Add SoundManagerHook.getNormalizedVolume(this, p_sound, soundpoolentry, soundcategory);
                            iterator.remove(); // Remove the old method call.
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

}

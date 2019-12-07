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

public class SoundManagerTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.audio.SoundManager}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.SoundManager.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.playSound.matches(methodNode)) {

                // Objective:
                // Find: this.getNormalizedVolume(p_sound, soundpoolentry, soundcategory);
                // Replace method with: SoundManagerHook.getNormalizedVolume(this, p_sound, soundpoolentry, soundcategory);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (nameMatches(methodInsnNode.owner, TransformerClass.SoundManager.getNameRaw()) && TransformerMethod.getNormalizedVolume.matches(methodInsnNode)) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/SoundManagerHook", "getNormalizedVolume",
                                    "("+TransformerClass.SoundManager.getName()+TransformerClass.ISound.getName()+TransformerClass.SoundPoolEntry.getName()+TransformerClass.SoundCategory.getName()+")F",
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

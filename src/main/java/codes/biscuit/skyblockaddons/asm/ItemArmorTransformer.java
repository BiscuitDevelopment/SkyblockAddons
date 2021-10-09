package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class ItemArmorTransformer implements ITransformer {

    /**
     * {@link net.minecraft.item.ItemArmor}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.ItemArmor.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.onItemRightClick.matches(methodNode)) {

                // Objective:
                // Remove "+ 1"

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();

                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.ICONST_1) {
                        if (iterator.hasNext()) {
                            AbstractInsnNode nextAbstractNode = iterator.next();

                            if (nextAbstractNode instanceof InsnNode && nextAbstractNode.getOpcode() == Opcodes.IADD) {
                                methodNode.instructions.remove(abstractNode);
                                methodNode.instructions.remove(nextAbstractNode);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}

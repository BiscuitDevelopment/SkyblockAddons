package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class ItemArmorDispenserBehaviorTransformer implements ITransformer {

    /**
     * {@link net.minecraft.item.ItemArmor}#dispenserBehavior
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.ItemArmor.getTransformerName()+"$1"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.dispenseStack.matches(methodNode)) {

                // Objective:
                // Replace "int l = 0;"
                // with: "int l = entitylivingbase instanceof EntityPlayer?1:0;"

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();

                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.ICONST_0) {
                        if (iterator.hasNext()) {
                            AbstractInsnNode nextAbstractNode = iterator.next();

                            if (nextAbstractNode instanceof VarInsnNode && nextAbstractNode.getOpcode() == Opcodes.ISTORE) {
                                methodNode.instructions.insertBefore(abstractNode, revertVanillaBehavior());

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

    private InsnList revertVanillaBehavior() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 9)); // entitylivingbase
        list.add(new TypeInsnNode(Opcodes.INSTANCEOF, TransformerClass.EntityPlayer.getNameRaw()));
        LabelNode notEqual = new LabelNode(); // if (entitylivingbase instanceof EntityPlayer) {
        list.add(new JumpInsnNode(Opcodes.IFEQ, notEqual));

        list.add(new InsnNode(Opcodes.ICONST_1)); // 1
        LabelNode afterCondition = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.GOTO, afterCondition));

        list.add(notEqual); // } else {
        list.add(new InsnNode(Opcodes.ICONST_0)); // 0

        list.add(afterCondition); // }
        list.add(new VarInsnNode(Opcodes.ISTORE, 10)); // l =

        return list;
    }
}

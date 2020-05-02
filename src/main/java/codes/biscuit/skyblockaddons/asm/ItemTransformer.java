package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class ItemTransformer implements ITransformer {

    /**
     * {@link net.minecraft.item.Item}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.Item.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.
            if (methodNode.name.equals("showDurabilityBar")) { // always deobfuscated

                // Objective:
                // Find: return stack.isItemDamaged();
                // Replace With: return ItemHook.isItemDamaged(stack);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.ItemStack.getNameRaw()) && TransformerMethod.isItemDamaged.matches(methodInsnNode)) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/ItemHook",
                                    "isItemDamaged", "("+ TransformerClass.ItemStack.getName()+")Z", false)); // ItemHook.isItemDamaged(stack);

                            iterator.remove(); // Remove the old line.
                            break;
                        }
                    }
                }
            }
            if (methodNode.name.equals("getDurabilityForDisplay")) { // always deobfuscated

                // Objective:
                // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           ItemHook.getDurabilityForDisplay(stack, returnValue);
                //           if (returnValue.isCancelled()) {
                //               return returnValue.getValue();
                //           }

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertDurabilityHook());
            }
        }
    }

    private InsnList insertDurabilityHook() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 2));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // stack
        list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // ItemHook.getDurabilityForDisplay(stack, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/ItemHook", "getDurabilityForDisplay",
                "("+TransformerClass.ItemStack.getName()+"Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 2));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new VarInsnNode(Opcodes.ALOAD, 2));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "getReturnValue",
                "()Ljava/lang/Object;", false));
        list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Double"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue",
                "()D", false));
        list.add(new InsnNode(Opcodes.DRETURN)); // return returnValue.getValue();
        list.add(notCancelled);

        return list;
    }
}

package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class EntityPlayerSPTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.entity.EntityPlayerSP}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.EntityPlayerSP.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {

            // Objective:
            // Find: Method head.
            // Insert:   ReturnValue returnValue = new ReturnValue();
            //           EntityPlayerSPHook.dropOneItemConfirmation(returnValue);
            //           if (returnValue.isCancelled()) {
            //               return null;
            //           }

            if (TransformerMethod.dropOneItem.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertConfirmation());
            }
        }
    }

    private InsnList insertConfirmation() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 3));

        list.add(new VarInsnNode(Opcodes.ALOAD, 3)); // EntityPlayerSPHook.dropOneItemConfirmation(returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/EntityPlayerSPHook", "dropOneItemConfirmation",
                "(Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)"+ TransformerClass.EntityItem.getName(), false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.ACONST_NULL)); // return null;
        list.add(new InsnNode(Opcodes.ARETURN));
        list.add(notCancelled);

        return list;
    }
}

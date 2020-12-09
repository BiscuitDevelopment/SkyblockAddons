package codes.biscuit.skyblockaddons.asm.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;

public class ASMUtils {

    public static FieldInsnNode getField(TransformerField field, TransformerClass targetClass) {
        return new FieldInsnNode(Opcodes.GETFIELD, targetClass.getNameRaw(), field.getName(), field.getType());
    }
}

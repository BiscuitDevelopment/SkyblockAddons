package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerField;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class EntityLivingBaseTransformer implements ITransformer {

    /**
     * {@link net.minecraft.entity.EntityLivingBase}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.EntityLivingBase.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.handleStatusUpdate.matches(methodNode)) {

                // Objective:
                // Find: this.hurtTime =
                // Insert After: EntityLivingBaseHook.onResetHurtTime();

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof FieldInsnNode && abstractNode.getOpcode() == Opcodes.PUTFIELD) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)abstractNode;
                        if (fieldInsnNode.owner.equals(TransformerClass.EntityLivingBase.getNameRaw()) &&
                                TransformerField.hurtTime.matches(fieldInsnNode)) {
                            methodNode.instructions.insert(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/EntityLivingBaseHook",
                                    "onResetHurtTime", "()V", false));
                        }
                    }
                }
            }
        }
    }
}

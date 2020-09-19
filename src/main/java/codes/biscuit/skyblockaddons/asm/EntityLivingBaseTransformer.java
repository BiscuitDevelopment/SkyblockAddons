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
                                    "onResetHurtTime", "("+TransformerClass.EntityLivingBase.getName()+")V", false));
                            methodNode.instructions.insert(abstractNode, new VarInsnNode(Opcodes.ALOAD, 0));
                        }
                    }
                }

            } else if (TransformerMethod.removePotionEffectClient.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), onRemovePotionEffect());

            } else if (TransformerMethod.addPotionEffect.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), onAddPotionEffect());
            }
        }
    }

    private InsnList onRemovePotionEffect() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // int potionId
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/EntityLivingBaseHook", "onRemovePotionEffect",
                "("+TransformerClass.EntityLivingBase.getName()+"I)Z", false));
        LabelNode notCancelled = new LabelNode(); // if (EntityLivingBaseHook.onRemovePotionEffect(this, potionId)) {
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled); // }

        return list;
    }

    private InsnList onAddPotionEffect() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // PotionEffect potioneffectIn // EntityLivingBaseHook.onAddPotionEffect(this, potioneffectIn);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/EntityLivingBaseHook", "onAddPotionEffect",
                "("+TransformerClass.EntityLivingBase.getName()+TransformerClass.PotionEffect.getName()+")V", false));

        return list;
    }
}

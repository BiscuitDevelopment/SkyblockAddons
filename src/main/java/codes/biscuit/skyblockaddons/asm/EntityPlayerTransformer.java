package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.ASMUtils;
import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerField;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class EntityPlayerTransformer implements ITransformer {

    /**
     * {@link net.minecraft.entity.player.EntityPlayer}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.EntityPlayer.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {

            // Objective:
            // Replace the entire method to fix a forge bug...
            if (TransformerMethod.setCurrentItemOrArmor.matches(methodNode)) {
                methodNode.instructions = setCurrentItemOrArmor();
            }
        }
    }

    private InsnList setCurrentItemOrArmor() {
        InsnList list = new InsnList();

        // this.inventory.armorInventory[slotIn] = stack;
        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(ASMUtils.getField(TransformerField.inventory, TransformerClass.EntityPlayer)); // this.inventory
        list.add(ASMUtils.getField(TransformerField.armorInventory, TransformerClass.InventoryPlayer)); // inventory.armorInventory
        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // slotIn
        list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // stack
        list.add(new InsnNode(Opcodes.AASTORE));
        list.add(new InsnNode(Opcodes.RETURN));

        return list;
    }
}

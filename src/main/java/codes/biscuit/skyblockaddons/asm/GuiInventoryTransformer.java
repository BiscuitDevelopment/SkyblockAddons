package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerField;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GuiInventoryTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.inventory.GuiInventory}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.GuiInventory.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {

        // Objective:
        // Add a new method to override handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType)
        //
        // @Override
        // public handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        //     ReturnValue returnValue = new ReturnValue();
        //     GuiInventoryHook.handleMouseClick(this.guiLeft, this.guiTop, this.oldMouseX, this.oldMouseY, this.xSize, this.ySize, returnValue);
        //     if (returnValue.isCancelled()) {
        //         return;
        //     }
        //     super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
        // }

        MethodNode handleMouseClick = TransformerMethod.handleMouseClick.createMethodNode();
        handleMouseClick.instructions.add(handleMouseClickBody());
        classNode.methods.add(handleMouseClick);
    }

    private InsnList handleMouseClickBody() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 5));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiLeft
        list.add(TransformerField.guiLeft.getField(TransformerClass.GuiInventory));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiTop
        list.add(TransformerField.guiTop.getField(TransformerClass.GuiInventory));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.oldMouseX
        list.add(TransformerField.oldMouseX.getField(TransformerClass.GuiInventory));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.oldMouseY
        list.add(TransformerField.oldMouseY.getField(TransformerClass.GuiInventory));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.xSize
        list.add(TransformerField.xSize.getField(TransformerClass.GuiInventory));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.ySize
        list.add(TransformerField.ySize.getField(TransformerClass.GuiInventory));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // GuiInventoryHook.handleMouseClick(this.guiLeft, this.guiTop, this.oldMouseX, this.oldMouseY, this.xSize, this.ySize, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiInventoryHook", "handleMouseClick",
                "(IIFFIILcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new VarInsnNode(Opcodes.ILOAD, 2));
        list.add(new VarInsnNode(Opcodes.ILOAD, 3));
        list.add(new VarInsnNode(Opcodes.ILOAD, 4)); // super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TransformerClass.GuiContainer.getNameRaw(), TransformerMethod.handleMouseClick.getName(),
                "("+TransformerClass.Slot.getName()+"III)V", false));

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }
}

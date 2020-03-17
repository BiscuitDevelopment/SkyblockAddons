package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerField;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class GuiIngameMenuTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.GuiIngameMenu}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.GuiIngameMenu.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.actionPerformed.matches(methodNode)) {

                // Objective 1:
                // Find: boolean flag = this.mc.isIntegratedServerRunning();
                // Insert Before: GuiDisconnectedHook.onDisconnect();

                // Objective 2:
                // Find: Head of actionPerformed.
                // Insert:
                // if (button.id == 53) {
                //     GuiIngameMenuHook.onButtonClick();
                // }

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertOnButtonClick());

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.Minecraft.getNameRaw()) &&
                                TransformerMethod.isIntegratedServerRunning.matches(methodInsnNode)) {

                            // Go two backwards because of this & this.mc.
                            methodNode.instructions.insertBefore(abstractNode.getPrevious().getPrevious(), new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "codes/biscuit/skyblockaddons/asm/hooks/GuiDisconnectedHook", "onDisconnect", "()V", false)); // GuiDisconnectedHook.onDisconnect();
                        }
                    }
                }
            } else if (TransformerMethod.initGui.matches(methodNode)) {

                // Objective:
                // Find: initGui() return.
                // Insert: GuiIngameMenuHook.addMenuButtons(this.buttonsList, this.width, this.height);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {

                        methodNode.instructions.insertBefore(abstractNode, insertAddMenuButtons());
                    }
                }
            }
        }
    }

    private InsnList insertAddMenuButtons() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.buttonsList
        list.add(TransformerField.buttonList.getField(TransformerClass.GuiIngameMenu));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.width
        list.add(TransformerField.width.getField(TransformerClass.GuiIngameMenu));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.height
        list.add(TransformerField.height.getField(TransformerClass.GuiIngameMenu));

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, // GuiIngameMenuHook.addMenuButtons(this.buttonsList, this.width, this.height);
                "codes/biscuit/skyblockaddons/asm/hooks/GuiIngameMenuHook", "addMenuButtons", "(Ljava/util/List;II)V", false));
        return list;
    }

    private InsnList insertOnButtonClick() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // button

        list.add(TransformerField.id.getField(TransformerClass.GuiButton)); // button.id

        list.add(new IntInsnNode(Opcodes.BIPUSH, 53));
        LabelNode labelNode = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IF_ICMPNE, labelNode)); // Jump to the label after the statement if they are not equal.

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"codes/biscuit/skyblockaddons/asm/hooks/GuiIngameMenuHook",
                "onButtonClick", "()V", false)); // GuiIngameMenuHook.onButtonClick();
        list.add(labelNode);

        return list;
    }
}

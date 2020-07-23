package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

public class GuiNewChatTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.GuiNewChat}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.GuiNewChat.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.printChatMessageWithOptionalDeletion.matches(methodNode)) {

                // Objective:
                // Find: chatComponent.getUnformattedText();
                // Replace With: GuiNewChatHook.getUnformattedText(chatComponent);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEINTERFACE) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.IChatComponent.getNameRaw()) && TransformerMethod.getUnformattedText.matches(methodInsnNode)) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiNewChatHook",
                                    "getUnformattedText", "("+TransformerClass.IChatComponent.getName()+")Ljava/lang/String;", false)); // GuiNewChatHook.getUnformattedText(chatComponent);

                            iterator.remove(); // Remove the old line.
                            break;
                        }
                    }
                }
            }
        }
    }
}

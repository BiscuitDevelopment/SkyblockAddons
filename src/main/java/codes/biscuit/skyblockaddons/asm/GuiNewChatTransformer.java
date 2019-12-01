package codes.biscuit.skyblockaddons.asm;

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
        return new String[]{"net.minecraft.client.gui.GuiNewChat"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode);
            if (nameMatches(methodName, "printChatMessageWithOptionalDeletion", "func_146234_a")) {

                // Objective:
                // Find: chatComponent.getUnformattedText();
                // Replace With: GuiNewChatHook.getUnformattedText(chatComponent);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEINTERFACE) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractNode;
                        System.out.println(methodInsnNode.owner+"|"+methodInsnNode.name);
                        if (nameMatches(methodInsnNode.owner, "net/minecraft/util/IChatComponent", "eu") && nameMatches(methodInsnNode.name, "getUnformattedText", "func_150260_c", "c")) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiNewChatHook",
                                    "getUnformattedText", "(Lnet/minecraft/util/IChatComponent;)Ljava/lang/String;", false)); // GuiNewChatHook.getUnformattedText(chatComponent);

                            iterator.remove(); // Remove the old line.
                            break;
                        }
                    }
                }
            }
        }
    }
}

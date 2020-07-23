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

public class MouseHelperTransformer implements ITransformer {

    /**
     * {@link net.minecraft.util.MouseHelper}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.MouseHelper.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.ungrabMouseCursor.matches(methodNode)) {

                // Objective:
                // Find: Mouse.setCursorPosition(MouseHelperHook.ungrabMouseCursor() / 2, Display.getHeight() / 2);
                // Replace method with: MouseHelperHook.ungrabMouseCursor(Display.getWidth() / 2, Display.getHeight() / 2);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESTATIC) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (methodInsnNode.owner.equals("org/lwjgl/input/Mouse") && methodInsnNode.name.equals("setCursorPosition")) { // these are not minecraft methods, not obfuscated

                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/MouseHelperHook",
                                    "ungrabMouseCursor", "(II)V", false)); // Add the replacement method call.
                            iterator.remove(); // Remove the old method call.
                            break;
                        }
                    }
                }
                break;
            }
        }
    }
}

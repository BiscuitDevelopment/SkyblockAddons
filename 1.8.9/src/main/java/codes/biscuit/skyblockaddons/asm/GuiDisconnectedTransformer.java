package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class GuiDisconnectedTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.GuiDisconnected}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.GuiDisconnected.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.init.matches(methodNode)) {

                // Objective:
                // Find: Constructor return.
                // Insert: GuiDisconnectedHook.onDisconnect();

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiDisconnectedHook",
                                "onDisconnect", "()V", false)); // GuiDisconnectedHook.onDisconnect();
                        break;
                    }
                }
            }
        }
    }
}

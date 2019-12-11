package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
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

                // Objective:
                // Find: boolean flag = this.mc.isIntegratedServerRunning();
                // Insert Before: GuiDisconnectedHook.onDisconnect();

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
                            break;
                        }
                    }
                }
            }
        }
    }
}

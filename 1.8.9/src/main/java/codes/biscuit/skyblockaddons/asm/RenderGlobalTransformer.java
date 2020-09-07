package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class RenderGlobalTransformer implements ITransformer {

    private LabelNode existingLabel = null;
    private LabelNode newLabel = new LabelNode();

    /**
     * {@link net.minecraft.client.renderer.RenderGlobal}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.RenderGlobal.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {

            if (TransformerMethod.renderEntities.matches(methodNode)) {

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractNode;
                        if (TransformerMethod.isRenderEntityOutlines.matches(methodInsnNode)) {

                            if (abstractNode.getNext() instanceof JumpInsnNode && abstractNode.getNext().getOpcode() == Opcodes.IFEQ) {
                                JumpInsnNode jumpInsnNode = (JumpInsnNode) abstractNode.getNext();

                                existingLabel = jumpInsnNode.label;
                                methodNode.instructions.insertBefore(abstractNode.getPrevious(), shouldRenderEntityOutlinesExtraCondition(newLabel));
                            }
                        }
                    }

                    if (newLabel != null && abstractNode instanceof LabelNode) {
                        if (abstractNode == existingLabel) {
                            methodNode.instructions.insertBefore(abstractNode, newLabel);
                            return;
                        }
                    }
                }
            } else if (TransformerMethod.isRenderEntityOutlines.matches(methodNode)) {

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), loadInFrameBuffers());

            } else if (TransformerMethod.renderEntityOutlineFramebuffer.matches(methodNode)) {
                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "codes/biscuit/skyblockaddons/asm/hooks/RenderGlobalHook", "afterFramebufferDraw", "()V", false));
                        break;
                    }
                }
            }
        }
    }

    private InsnList shouldRenderEntityOutlinesExtraCondition(LabelNode labelNode) {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // camera
        list.add(new VarInsnNode(Opcodes.FLOAD, 3)); // partialTicks
        list.add(new VarInsnNode(Opcodes.DLOAD, 5)); // x
        list.add(new VarInsnNode(Opcodes.DLOAD, 7)); // y
        list.add(new VarInsnNode(Opcodes.DLOAD, 9)); // z
        list.add(new VarInsnNode(Opcodes.ALOAD, 18)); // list
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/RenderGlobalHook", "blockRenderingSkyblockItemOutlines", "("+TransformerClass.ICamera.getName()+"FDDDLjava/util/List;)Z", false));
        list.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));

        return list;
    }

    private InsnList loadInFrameBuffers() {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/RenderGlobalHook", "shouldRenderSkyblockItemOutlines", "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (RenderGlobalHook.shouldRenderSkyblockItemOutlines())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.ICONST_1)); // return true;
        list.add(new InsnNode(Opcodes.IRETURN));
        list.add(notCancelled);

        return list;
    }
}
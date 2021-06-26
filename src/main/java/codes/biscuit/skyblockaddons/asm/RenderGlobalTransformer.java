package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.hooks.RenderGlobalHook;
import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.BlockPos;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class RenderGlobalTransformer implements ITransformer {

    private LabelNode existingLabel = null;
    private final LabelNode newLabel = new LabelNode();

    /**
     * {@link net.minecraft.client.renderer.RenderGlobal}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.RenderGlobal.getTransformerName()};
    }

    /**
     * See {@link RenderGlobalHook#blockRenderingSkyblockItemOutlines(ICamera, float, double, double, double)},
     * {@link RenderGlobalHook#onAddBlockBreakParticle(int, BlockPos, int)}, and
     * {@link RenderGlobalHook#shouldRenderSkyblockItemOutlines()})
     */
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
                        }
                    }
                }
            } else if (TransformerMethod.isRenderEntityOutlines.matches(methodNode)) {

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), loadInFrameBuffers());

            } else if (TransformerMethod.sendBlockBreakProgress.matches(methodNode)) {

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertOnAddBlockBreakParticle());

            }
        }
    }

    private InsnList insertOnAddBlockBreakParticle() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ILOAD, 1));
        list.add(new VarInsnNode(Opcodes.ALOAD, 2));
        list.add(new VarInsnNode(Opcodes.ILOAD, 3));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "codes/biscuit/skyblockaddons/asm/hooks/RenderGlobalHook", "onAddBlockBreakParticle", "(I"+TransformerClass.BlockPos.getName()+"I)V", false));
        return list;
    }

    private InsnList shouldRenderEntityOutlinesExtraCondition(LabelNode labelNode) {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // camera
        list.add(new VarInsnNode(Opcodes.FLOAD, 3)); // partialTicks
        list.add(new VarInsnNode(Opcodes.DLOAD, 5)); // x
        list.add(new VarInsnNode(Opcodes.DLOAD, 7)); // y
        list.add(new VarInsnNode(Opcodes.DLOAD, 9)); // z
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/RenderGlobalHook", "blockRenderingSkyblockItemOutlines", "(" + TransformerClass.ICamera.getName() + "FDDD)Z", false));
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
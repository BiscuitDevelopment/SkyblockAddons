package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class RenderManagerTransformer implements ITransformer {


    /**
     * {@link net.minecraft.client.renderer.entity.RenderManager}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.RenderManager.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.shouldRender.matches(methodNode)) {

                // Objective:
                // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           RenderManagerHook.shouldRender(entityIn, returnValue);
                //           if (returnValue.isCancelled()) {
                //               return false;
                //           }

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertShouldRender());
            }
        }
    }

    private InsnList insertShouldRender() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 10));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // entityIn
        list.add(new VarInsnNode(Opcodes.ALOAD, 10)); //RenderManagerHook.shouldRender(entityIn, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/RenderManagerHook", "shouldRender",
                "("+TransformerClass.Entity.getName()+"Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 10));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.ICONST_0)); // return false;
        list.add(new InsnNode(Opcodes.IRETURN));
        list.add(notCancelled);

        return list;
    }
}

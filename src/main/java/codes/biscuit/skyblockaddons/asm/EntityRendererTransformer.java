package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class EntityRendererTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.renderer.EntityRenderer}
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.renderer.EntityRenderer"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode); // Map all of the method names.
            if (nameMatches(methodName,"getMouseOver", "func_78473_a")) {

                // Objective:
                // Find: The entity list variable.
                // Insert EntityRendererHook.removeEntities(list);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof VarInsnNode && abstractNode.getOpcode() == Opcodes.DLOAD) {
                        VarInsnNode varInsnNode = (VarInsnNode)abstractNode;
                        if (varInsnNode.var == 5) { // List variable is created right before variable 5 is accessed (double d3 = d2;)
                            methodNode.instructions.insertBefore(varInsnNode, insertRemoveEntities());
                            break;
                        }
                    }
                }
            } else if (nameMatches(methodName,"getNightVisionBrightness", "func_180438_a")) {

                // Objective:
                // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           EntityPlayerSPHook.preventBlink(returnValue);
                //           if (returnValue.isCancelled()) {
                //               return 1.0F;
                //           }

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertNightVision());
            }
        }
    }

    private InsnList insertRemoveEntities() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 14)); // EntityRendererHook.removeEntities(list);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/EntityRendererHook", "removeEntities",
                "(Ljava/util/List;)V", false));

        return list;
    }

    private InsnList insertNightVision() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 4));

        list.add(new VarInsnNode(Opcodes.ALOAD, 4)); // EntityRendererHook.preventBlink(returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/EntityRendererHook", "preventBlink",
                "(Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 4));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.FCONST_1)); // return 1.0F;
        list.add(new InsnNode(Opcodes.FRETURN));
        list.add(notCancelled);

        return list;
    }
}

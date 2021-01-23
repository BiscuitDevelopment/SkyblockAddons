package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.InjectionHelper;
import codes.biscuit.skyblockaddons.asm.utils.InstructionBuilder;
import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PlayerControllerMPTransformer implements ITransformer {


    /**
     * {@link net.minecraft.client.multiplayer.PlayerControllerMP}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.PlayerControllerMP.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
           if (InjectionHelper.matches(methodNode, TransformerMethod.onPlayerDestroyBlock)) {

               InjectionHelper.start()
                       .matchingOwner(TransformerClass.World).matchingMethod(TransformerMethod.playAuxSFX).endCondition()

                       .injectCodeBefore()
                           .load(InstructionBuilder.VariableType.OBJECT, 1) // loc
                           // PlayerControllerMPHook.onPlayerDestroyBlock(loc);
                           .callStaticMethod("codes/biscuit/skyblockaddons/asm/hooks/PlayerControllerMPHook", "onPlayerDestroyBlock", "("+ TransformerClass.BlockPos.getName()+")V")
                           .endCode()
                       .finish();

            } else if (TransformerMethod.windowClick.matches(methodNode)) {

                // Objective:
                // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           PlayerControllerMPHook.onWindowClick(slotId, mode, playerIn, returnValue);
                //           if (returnValue.isCancelled()) {
                //               return null;
                //           }

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertOnWindowClick());

            }
        }
    }

    private InsnList insertOnWindowClick() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 8));

        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // slotId
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // mouseButtonClicked
        list.add(new VarInsnNode(Opcodes.ILOAD, 4)); // mode
        list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // playerIn
        list.add(new VarInsnNode(Opcodes.ALOAD, 8)); // PlayerControllerMPHook.onWindowClick(slotId, mouseButtonClicked, mode, playerIn, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/PlayerControllerMPHook", "onWindowClick",
               "(III"+TransformerClass.EntityPlayer.getName()+"Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 8));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.ACONST_NULL)); // return null;
        list.add(new InsnNode(Opcodes.ARETURN));
        list.add(notCancelled);

        return list;
    }
}

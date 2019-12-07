package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class NetHandlerPlayClientTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.network.NetHandlerPlayClient}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.NetHandlerPlayClient.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.handleSetSlot.matches(methodNode)) {

                // Objective:
                // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           NetHanderPlayClientHook.handleSetSlot(packetIn, returnValue);
                //           if (returnValue.isCancelled()) {
                //               return;
                //           }

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertHandleSetSlot());
            }
            if (TransformerMethod.handleWindowItems.matches(methodNode)) {

                // Objective:
                // Find: Method head.
                // Insert: NetHanderPlayClientHook.handleWindowItems(packetIn);

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertHandleWindowItems());
            }
        }
    }

    private InsnList insertHandleSetSlot() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 5));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // packetIn
        list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // NetHanderPlayClientHook.handleSetSlot(packetIn, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/NetHanderPlayClientHook", "handleSetSlot",
                "("+TransformerClass.S2FPacketSetSlot+"Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

        return list;
    }

    private InsnList insertHandleWindowItems() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // packetIn
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/NetHanderPlayClientHook", "handleWindowItems",
                "("+TransformerClass.S30PacketWindowItems+")V", false));

        return list;
    }
}

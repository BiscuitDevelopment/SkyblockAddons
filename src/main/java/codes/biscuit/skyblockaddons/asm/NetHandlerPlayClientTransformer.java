package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class NetHandlerPlayClientTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.network.NetHandlerPlayClient}
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.network.NetHandlerPlayClient"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode); // Map all of the method names.
            if (nameMatches(methodName, "handleSetSlot", "func_147266_a")) {

                // Objective:
                // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           NetHanderPlayClientHook.handleSetSlot(packetIn, returnValue);
                //           if (returnValue.isCancelled()) {
                //               return;
                //           }

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertHandleSetSlot());
            }
            if (nameMatches(methodName, "handleWindowItems", "func_147241_a")) {

                // Objective:
                // Find: Method head.
                // Insert: NetHanderPlayClientHook.handleWindowItems(packetIn);

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertHandleWindowItems());
            }
        }
    }

    private InsnList insertHandleSetSlot() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 5));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // packetIn
        list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // NetHanderPlayClientHook.handleSetSlot(packetIn, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/NetHanderPlayClientHook", "handleSetSlot",
                "(Lnet/minecraft/network/play/server/S2FPacketSetSlot;Lcodes/biscuit/skyblockaddons/asm/hooks/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "isCancelled",
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
                "(Lnet/minecraft/network/play/server/S30PacketWindowItems;)V", false));

        return list;
    }
}

package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class FontRendererTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.FontRenderer}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.FontRenderer.getTransformerName(), "club.sk1er.patcher.hooks.FontRendererHook"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {

            // Objective:
            // Find Method Head: Add:
            //   FontRendererHook.changeTextColor(); <- insert the call right before the return

            // TODO Test with patcher...
            if (TransformerMethod.renderChar.matches(methodNode) || methodNode.name.equals("renderChar")) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "changeTextColor", "()V", false));
            }

            // Add chroma format to the font renderer with format code §z
            if (TransformerMethod.renderStringAtPos.matches(methodNode)) {
                boolean noComplications = false;
                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();

                    // Add the color code "z" in the 22nd index (line 375)
                    if (abstractNode instanceof LdcInsnNode && abstractNode.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode) abstractNode;
                        if (ldcInsnNode.cst instanceof String && ldcInsnNode.cst.equals("0123456789abcdefklmnor")) {
                            ldcInsnNode.cst = "0123456789abcdefklmnorz";
                            // Only insert the following instructions if we were able to insert here
                            noComplications = true;
                        }
                    }
                    // Find the if statement on line 379: if (i1 < 16) { }
                    else if (noComplications && abstractNode instanceof LineNumberNode && ((LineNumberNode) abstractNode).line == 379) {
                        // Insert chroma toggle off on line 379 if (i1 < 16) { FontRendererHook.toggleChromaOff(); ... }
                        methodNode.instructions.insert(abstractNode,
                                new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "toggleChromaOff", "()V", false));
                        System.out.println("Success 16");
                    }
                    // Find the if statement on line 421: if (i1 == 21) { }
                    else if (noComplications && abstractNode instanceof LineNumberNode && ((LineNumberNode) abstractNode).line == 421) {
                        methodNode.instructions.insert(abstractNode,
                                new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "toggleChromaOff", "()V", false));

                        // Get the GOTO label if (i1 != 21)
                        AbstractInsnNode twoNodesBefore = abstractNode.getPrevious() != null ? abstractNode.getPrevious().getPrevious() : null;
                        if (twoNodesBefore instanceof JumpInsnNode && twoNodesBefore.getOpcode() == Opcodes.IF_ICMPNE) {
                            // Insert before the end
                            LabelNode endIf = ((JumpInsnNode) twoNodesBefore).label;
                            methodNode.instructions.insert(endIf, checkChromaToggleOn());
                            System.out.println("Success 22");
                        }
                        System.out.println("Success 21");
                    }
                    else if (noComplications && abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode,
                                new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "restoreShaderState", "()V", false));
                    }
                }
                if (noComplications) {
                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "saveShaderState", "()V", false));
                System.out.println("Success!");
                }

            }
        }
    }


    /**
     * Insert instructions on line 428:
     * else if (i1 == 22) {
     *     this.resetStyles();
     *     FontRenderHook.toggleChromaOn();
     * }
     */
    private InsnList checkChromaToggleOn() {
        InsnList list = new InsnList();

        LabelNode endIf = new LabelNode();
        // if (i1 == 22) {}
        list.add(new VarInsnNode(Opcodes.ILOAD, 5));
        list.add(new IntInsnNode(Opcodes.BIPUSH, 22));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPNE, endIf));

        // this.resetStyles()
        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TransformerClass.FontRenderer.getNameRaw(), TransformerMethod.resetStyles.getName(), TransformerMethod.resetStyles.getDescription(), false));

        // Call shader manager
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "toggleChromaOn", "()V", false));

        list.add(endIf);

        return list;
    }


    /*else if (noComplications && abstractNode instanceof VarInsnNode && abstractNode.getOpcode() == Opcodes.ILOAD && ((VarInsnNode) abstractNode).var == 5) {
                        AbstractInsnNode nextNode = abstractNode.getNext();
                        if (nextNode instanceof IntInsnNode && nextNode.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) nextNode).operand == 21) {
                            nextNode = nextNode.getNext();
                            if (nextNode instanceof JumpInsnNode && nextNode.getOpcode() == Opcodes.IF_ICMPNE) {

     */
}
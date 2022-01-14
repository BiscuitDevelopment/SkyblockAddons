package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.hooks.FontRendererHook;
import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerField;
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

            if (classNode.name.equals("club/sk1er/patcher/hooks/FontRendererHook")) {
                if (methodNode.name.equals("renderStringAtPos") && methodNode.desc.equals("(Ljava/lang/String;Z)Z")) {
                    // Objective:
                    // Find Method Head: Add:
                    //   if (FontRendererHook.shouldOverridePatcher(text)) return false;
                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), patcherOverride());
                }
                continue;
            }

            // Objective:
            // Find Method Head: Add:
            //   FontRendererHook.changeTextColor(); <- insert the call right before the return

            if (TransformerMethod.renderChar.matches(methodNode) || methodNode.name.equals("renderChar")) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertChangeTextColor());
            }


            // Objective:
            // Add color code §z to toggle a chroma mode for font rendering
            // Stop patcher code from running if the text has a chroma colorcode
            else if (TransformerMethod.renderStringAtPos.matches(methodNode)) {

                FieldInsnNode fieldInsnNode;
                AbstractInsnNode nextNode;

                LabelNode elseIf22Start = new LabelNode();
                LabelNode nextElseIf = null;

                // In vanilla, this is always the last on the style list
                int italicStyleCount = 0;
                boolean findString = false;
                boolean insertedChroma = false;
                boolean findIfEq20 = false;

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();

                    // Add the §z color code in the 22nd index
                    if (!findString &&
                            abstractNode instanceof LdcInsnNode && abstractNode.getOpcode() == Opcodes.LDC &&
                            ((LdcInsnNode) abstractNode).cst instanceof String && ((LdcInsnNode) abstractNode).cst.equals("0123456789abcdefklmnor")) {
                        ((LdcInsnNode) abstractNode).cst = "0123456789abcdefklmnorz";
                        findString = true;
                    }
                    // Find calls to fontRenderer.italicStyle = ___ and insert chroma toggle off after the 1st and 3rd call
                    else if (findString &&
                            abstractNode instanceof FieldInsnNode && (fieldInsnNode = (FieldInsnNode)abstractNode).getOpcode() == Opcodes.PUTFIELD &&
                            fieldInsnNode.owner.equals(TransformerClass.FontRenderer.getNameRaw()) && fieldInsnNode.name.equals(TransformerField.italicStyle.getName())) {
                        italicStyleCount++;
                        // Insert a chroma reset, as the new format code is between 0 and 15 (regular colors)
                        if (italicStyleCount == 1 || italicStyleCount == 3) {
                            methodNode.instructions.insert(abstractNode, insertRestoreChromaState());
                        }
                    }
                    // Find if (i1 == 20) { }
                    else if (findString && !findIfEq20 &&
                            abstractNode instanceof VarInsnNode && abstractNode.getOpcode() == Opcodes.ILOAD && ((VarInsnNode)abstractNode).var == 5 &&
                            (nextNode = abstractNode.getNext()) instanceof IntInsnNode && nextNode.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) nextNode).operand == 20 &&
                            (nextNode = nextNode.getNext()) instanceof JumpInsnNode && nextNode.getOpcode() == Opcodes.IF_ICMPNE) {
                        nextElseIf = ((JumpInsnNode) nextNode).label;
                        ((JumpInsnNode) nextNode).label = elseIf22Start;
                        findIfEq20 = true;
                    }
                    // Get the first GOTO label after if (i1 == 20), which is where we'll insert a second else-if (i1 == 22) {turnOnChroma()} call
                    else if (findIfEq20 && !insertedChroma &&
                            abstractNode instanceof JumpInsnNode && abstractNode.getOpcode() == Opcodes.GOTO) {
                        methodNode.instructions.insert(abstractNode, checkChromaToggleOn(elseIf22Start, nextElseIf, ((JumpInsnNode) abstractNode).label));
                        insertedChroma = true;
                    }
                    // Insert a call to FontRendererHook.restoreChromaState() before calls to return
                    else if (insertedChroma && abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, insertEndOfString());
                        //methodNode.instructions.insertBefore(abstractNode, saveStringChroma());
                    }
                }
                // Only do this if the initial injection was successful
                if (insertedChroma) {
                    // Insert a call to FontRendererHook.beginRenderString(shadow) as the first instruction
                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertBeginRenderString());
                }
            }
        }
    }

    /**
     * Inserts a call to {@link FontRendererHook#changeTextColor()}
     */
    private InsnList insertChangeTextColor() {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "changeTextColor", "()V", false));

        return list;
    }

    /**
     * Inserts a call to {@link FontRendererHook#beginRenderString(boolean)}
     */
    private InsnList insertBeginRenderString() {
        InsnList list = new InsnList();

        // FontRendererHook.beginRenderString(shadow);
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // shadow
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "beginRenderString", "(Z)V", false));

        return list;
    }

    /**
     * Skips patcher's optimized font renderer if the call to {@link FontRendererHook#shouldOverridePatcher(String)} returns true
     */
    private InsnList patcherOverride() {
        InsnList list = new InsnList();

        // if (FontRendererHook.shouldOverridePatcher(text)) return false;
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "shouldOverridePatcher", "(Ljava/lang/String;)Z", false));
        LabelNode endIf = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, endIf));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new InsnNode(Opcodes.IRETURN));
        list.add(endIf);

        return list;
    }

    /**
     * Inserts a call to {@link FontRendererHook#restoreChromaState()}
     */
    private InsnList insertRestoreChromaState() {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "restoreChromaState", "()V", false));

        return list;
    }

    /**
     * Inserts a call to {@link FontRendererHook#endRenderString()}
     */
    private InsnList insertEndOfString() {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "endRenderString", "()V", false));

        return list;
    }


    /**
     * Insert instructions on line 419:
     * else if (i1 == 22) {
     * this.resetStyles();
     * {@link FontRendererHook#toggleChromaOn}
     * }
     */
    private InsnList checkChromaToggleOn(LabelNode startIf, LabelNode elseIf, LabelNode endIf) {
        InsnList list = new InsnList();

        list.add(startIf);
        list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        // else if (i1 == 22) {}
        list.add(new VarInsnNode(Opcodes.ILOAD, 5));
        list.add(new IntInsnNode(Opcodes.BIPUSH, 22));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPNE, elseIf));

        // this.resetStyles()
        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TransformerClass.FontRenderer.getNameRaw(), TransformerMethod.resetStyles.getName(), TransformerMethod.resetStyles.getDescription(), false));

        // Call shader manager
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "toggleChromaOn", "()V", false));

        // Go to end of else if chain
        list.add(new JumpInsnNode(Opcodes.GOTO, endIf));

        return list;
    }
}
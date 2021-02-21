package codes.biscuit.skyblockaddons.asm;

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

            // Objective:
            // Find Method Head: Add:
            //   FontRendererHook.changeTextColor(); <- insert the call right before the return

            if (TransformerMethod.renderChar.matches(methodNode) || methodNode.name.equals("renderChar")) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "changeTextColor", "()V", false));
            }

            // Objective:
            // Disable patcher's font optimization if FontRendererHook.shouldOverridePatcher(text)
            if (name.equals("club.sk1er.patcher.hooks.FontRendererHook") && methodNode.name.equals("renderStringAtPos")) {

                AbstractInsnNode nextNode;
                boolean inserted = false;
                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (!inserted && abstractNode instanceof FieldInsnNode && abstractNode.getOpcode() == Opcodes.GETFIELD &&
                            ((FieldInsnNode) abstractNode).owner.equals("net/minecraft/client/gui/FontRenderer") && ((FieldInsnNode) abstractNode).name.equals("field_78298_i") &&
                            (nextNode = abstractNode.getNext()) instanceof JumpInsnNode && nextNode.getOpcode() == Opcodes.IFNULL) {
                        methodNode.instructions.insert(nextNode, patcherOverride(((JumpInsnNode) nextNode).label));
                        inserted = true;
                    }
                    if (abstractNode instanceof FrameNode) {
                        System.out.println(((FrameNode)abstractNode).type);
                    }
                }

                // Objective:
                // Hook into Patcher's font renderer and insert chroma shader on/off when §z color code is found
                // TODO: Not really sure if patcher's glList optimization saves shader changes, in which case this doesn't work at all
                // TODO: Patcher's code doesn't seem to have StackMap Frames. Turning on COMPUTE_FRAMES ignores the function; adding new branches causes Expected stack error
                /*


                LabelNode elseIf22Start = new LabelNode();
                LabelNode nextElseIf = null;

                // In vanilla, this is always the last on the style list
                LdcInsnNode ldcInsnNode;
                FieldInsnNode fieldInsnNode;

                int styleIndex = -1;
                int valueIndex = 0;

                // In patcher, this is always the last on the style list (field_78302_t)
                int boldStyleCount = 0;
                boolean findString = false;
                boolean findStyleIndex = false;
                boolean findValueIndex = false;
                boolean insertedChroma = false;
                boolean findIfEq20 = false;


                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();


                    // Step 1: Add the color code "z" in the 22nd index
                    if (!findString && abstractNode instanceof LdcInsnNode && (ldcInsnNode = ((LdcInsnNode) abstractNode)).cst instanceof String &&
                            ldcInsnNode.cst.equals("0123456789abcdefklmnor")) {
                        ldcInsnNode.cst = "0123456789abcdefklmnorz";
                        findString = true;
                        System.out.println("Success 1");
                    }
                    // Step 2: Get the local variable table index for styleIndex by finding the next ISTORE after foundString
                    else if (findString && !findStyleIndex && abstractNode instanceof VarInsnNode && abstractNode.getOpcode() == Opcodes.ISTORE) {
                        styleIndex = ((VarInsnNode) abstractNode).var;
                        findStyleIndex = true;
                        System.out.println("Success 2");
                    }
                    else if (findString && !findValueIndex &&
                            abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                            ((MethodInsnNode) abstractNode).owner.equals("club/sk1er/patcher/util/enhancement/text/CachedString") &&
                            (abstractNode = abstractNode.getPrevious().getPrevious()) instanceof VarInsnNode && abstractNode.getOpcode() == Opcodes.ALOAD) {
                        valueIndex = ((VarInsnNode) abstractNode).var;
                        findValueIndex = true;
                        System.out.println("Success 3");
                    }
                    // Find calls to fontRenderer.boldStyle = ___
                    else if (findStyleIndex &&
                            abstractNode instanceof FieldInsnNode && (fieldInsnNode = (FieldInsnNode)abstractNode).getOpcode() == Opcodes.PUTFIELD &&
                            fieldInsnNode.owner.equals("net/minecraft/client/gui/FontRenderer") && fieldInsnNode.name.equals("field_78302_t")) {
                        boldStyleCount++;
                        // Insert a chroma reset, as the new format code is between 0 and 15 (regular colors)
                        if (boldStyleCount == 1 || boldStyleCount == 3) {
                            methodNode.instructions.insert(abstractNode,
                                    new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "toggleChromaOff", "()V", false));
                            System.out.println("Success 4");
                        }
                    }

                    // Find: if (styleIndex == 20) { }
                    else if (findStyleIndex && findValueIndex && !findIfEq20 &&
                            abstractNode instanceof VarInsnNode && abstractNode.getOpcode() == Opcodes.ILOAD && ((VarInsnNode)abstractNode).var == styleIndex &&
                            (nextNode = abstractNode.getNext()) instanceof IntInsnNode && nextNode.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) nextNode).operand == 20 &&
                            (nextNode = nextNode.getNext()) instanceof JumpInsnNode && nextNode.getOpcode() == Opcodes.IF_ICMPNE) {
                        nextElseIf = ((JumpInsnNode) nextNode).label;
                        ((JumpInsnNode) nextNode).label = elseIf22Start;
                        findIfEq20 = true;
                        System.out.println("Success 5");
                    }
                    // Get the first GOTO label after if (i1 == 20), which is where we'll insert a second else-if (i1 == 22) {turnOnChroma()} call
                    else if (findIfEq20 && !insertedChroma &&
                            abstractNode instanceof JumpInsnNode && abstractNode.getOpcode() == Opcodes.GOTO) {
                        methodNode.instructions.insert(abstractNode, checkChromaToggleOnPatcher(elseIf22Start, nextElseIf, ((JumpInsnNode) abstractNode).label, styleIndex, valueIndex));
                        insertedChroma = true;
                        System.out.println("Success 6");
                    }
                }
            */
            }


            // Objective:
            // Add color code §z to toggle a chroma mode for font rendering
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
                            methodNode.instructions.insert(abstractNode,
                                    new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "toggleChromaOff", "()V", false));
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
                    // Insert a call to FontRendererHook.restoreShaderState() before calls to return
                    else if (insertedChroma && abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode,
                                new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "restoreShaderState", "()V", false));
                        methodNode.instructions.insertBefore(abstractNode, saveStringChroma());
                    }
                }
                // Insert a call to FontRendererHook.saveShaderSate() as the first instruction
                if (insertedChroma) {
                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "saveShaderState", "()V", false));
                }
            }
        }
    }

    private InsnList patcherOverride(LabelNode endIf) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "shouldOverridePatcher", "(Ljava/lang/String;)Z", false));
        list.add(new JumpInsnNode(Opcodes.IFNE, endIf));
        return list;
    }

    /**
     * Insert instructions on line 419:
     * else if (i1 == 22) {
     *     this.resetStyles();
     *     FontRenderHook.toggleChromaOn();
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

        // Save string chroma
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "stringWithChroma", "(Ljava/lang/String;)V", false));

        // Go to end of else if chain
        list.add(new JumpInsnNode(Opcodes.GOTO, endIf));

        return list;
    }


    private static InsnList saveStringChroma() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "endOfString", "(Ljava/lang/String;)V", false));
        return list;
    }

    /*
     * Insert instructions
     * if (styleIndex == 22) {
     *     FontRenderHook.toggleChromaOn();
     *     value.setLastGreen(1F);
     *     value.setLastAlpha(1F);
     *     value.setLastBlue(1F);
     *     value.setLastRed(1F); // AKA white
     * }
     * else {
     *     FontRendererHook.toggleChromaOff();
     *     ....
     * }
     */
    /*
    private InsnList checkChromaToggleOnPatcher(LabelNode startIf, LabelNode elseIf, LabelNode endIf, int styleIndex, int valueIndex) {
        InsnList list = new InsnList();

        list.add(startIf);
        list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        // if (i1 == 22) {}
        list.add(new VarInsnNode(Opcodes.ILOAD, styleIndex));
        list.add(new IntInsnNode(Opcodes.BIPUSH, 22));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPNE, elseIf));

        // this.fontRenderer.field_78299_w = false
        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "club/sk1er/patcher/hooks/FontRendererHook", "fontRenderer", "Lnet/minecraft/client/gui/FontRenderer;"));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/gui/FontRenderer", "field_78299_w", "Z"));
        // this.fontRenderer.field_78300_v = false
        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "club/sk1er/patcher/hooks/FontRendererHook", "fontRenderer", "Lnet/minecraft/client/gui/FontRenderer;"));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/gui/FontRenderer", "field_78300_v", "Z"));
        // this.fontRenderer.field_78301_u = false
        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "club/sk1er/patcher/hooks/FontRendererHook", "fontRenderer", "Lnet/minecraft/client/gui/FontRenderer;"));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/gui/FontRenderer", "field_78301_u", "Z"));
        // this.fontRenderer.field_78303_s = false
        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "club/sk1er/patcher/hooks/FontRendererHook", "fontRenderer", "Lnet/minecraft/client/gui/FontRenderer;"));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/gui/FontRenderer", "field_78303_s", "Z"));
        // this.fontRenderer.field_78302_t = false
        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "club/sk1er/patcher/hooks/FontRendererHook", "fontRenderer", "Lnet/minecraft/client/gui/FontRenderer;"));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/gui/FontRenderer", "field_78302_t", "Z"));

        // Call shader manager
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "toggleChromaOn", "()V", false));
        // value.setLastGreen(1F);
        list.add(new VarInsnNode(Opcodes.ALOAD, valueIndex));
        list.add(new InsnNode(Opcodes.FCONST_1));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "club/sk1er/patcher/util/enhancement/text/CachedString", "setLastGreen", "(F)V", false));
        // value.setLastAlpha(1F);
        list.add(new VarInsnNode(Opcodes.ALOAD, valueIndex));
        list.add(new InsnNode(Opcodes.FCONST_1));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "club/sk1er/patcher/util/enhancement/text/CachedString", "setLastAlpha", "(F)V", false));
        // value.setLastBlue(1F);
        list.add(new VarInsnNode(Opcodes.ALOAD, valueIndex));
        list.add(new InsnNode(Opcodes.FCONST_1));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "club/sk1er/patcher/util/enhancement/text/CachedString", "setLastBlue", "(F)V", false));
        // value.setLastRed(1F);
        list.add(new VarInsnNode(Opcodes.ALOAD, valueIndex));
        list.add(new InsnNode(Opcodes.FCONST_1));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "club/sk1er/patcher/util/enhancement/text/CachedString", "setLastRed", "(F)V", false));

        list.add(new JumpInsnNode(Opcodes.GOTO, endIf));

        return list;
    }
    */
}
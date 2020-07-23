package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class GuiIngameCustomTransformer implements ITransformer {

    private boolean foundHealthBlock;
    private boolean foundFoodBlock;

    private boolean doneHealth;
    private boolean doneFood;
    private boolean doneArmor;
    private boolean doneMountHealth;

    /**
     * Labymod: net.labymod.core_implementation.mc18.gui.GuiIngameCustom
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.labymod.core_implementation.mc18.gui.GuiIngameCustom"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("renderPlayerStatsNew")) {

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();

                    if (abstractNode instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode) abstractNode;
                        if (!doneArmor && "armor".equals(ldcInsnNode.cst)) {
                            methodNode.instructions.insert(ldcInsnNode.getNext(), insertCancelArmorRendering());
                            doneArmor = true;

                        } else if ("health".equals(ldcInsnNode.cst)) {
                            foundHealthBlock = true;

                        } else if ("food".equals(ldcInsnNode.cst)) {
                            foundFoodBlock = true;

                        }
                    }

                    if (abstractNode instanceof JumpInsnNode) {
                        JumpInsnNode jumpInsnNode = (JumpInsnNode) abstractNode;
                        if (!doneHealth && foundHealthBlock && abstractNode.getOpcode() == Opcodes.IFLT) {
                            doneHealth = true;
                            methodNode.instructions.insert(abstractNode, insertCancelHealthRendering(jumpInsnNode.label));
                        }

                        if (!doneFood && abstractNode.getOpcode() == Opcodes.IFNONNULL) {
                            doneFood = true;
                            methodNode.instructions.insert(abstractNode, insertCancelFoodRendering(jumpInsnNode.label));
                        }
                    }

                    if (!doneMountHealth && foundFoodBlock && abstractNode instanceof TypeInsnNode && abstractNode.getOpcode() == Opcodes.INSTANCEOF) {
                        TypeInsnNode typeInsnNode = (TypeInsnNode) abstractNode;

                        if (typeInsnNode.desc.equals(TransformerClass.EntityLivingBase.getNameRaw()) &&
                                typeInsnNode.getNext().getOpcode() == Opcodes.IFEQ && typeInsnNode.getNext() instanceof JumpInsnNode) {
                            JumpInsnNode jumpInsnNode = (JumpInsnNode) typeInsnNode.getNext();

                            doneMountHealth = true;
                            methodNode.instructions.insert(jumpInsnNode, insertCancelMountHealthRendering(jumpInsnNode.label));
                        }
                    }
                }
            }
        }
    }

    private InsnList insertCancelArmorRendering() {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiIngameCustomHook", "shouldRenderArmor", "()Z", false));

        LabelNode notCancelled = new LabelNode(); // if (!GuiIngameCustomHook.shouldRenderArmor())
        list.add(new JumpInsnNode(Opcodes.IFNE, notCancelled));

        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new VarInsnNode(Opcodes.ISTORE, 22)); // k2 = 0;

        list.add(notCancelled); // }

        return list;
    }

    private InsnList insertCancelHealthRendering(LabelNode label) {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiIngameCustomHook", "shouldRenderHealth", "()Z", false));
        list.add(new JumpInsnNode(Opcodes.IFEQ, label)); // && shouldRenderHealth()

        return list;
    }

    private InsnList insertCancelFoodRendering(LabelNode label) {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiIngameCustomHook", "shouldRenderFood", "()Z", false));
        list.add(new JumpInsnNode(Opcodes.IFEQ, label));  // && shouldRenderFood()

        return list;
    }

    private InsnList insertCancelMountHealthRendering(LabelNode label) {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiIngameCustomHook", "shouldRenderMountHealth", "()Z", false));
        list.add(new JumpInsnNode(Opcodes.IFEQ, label));  // && shouldRenderMountHealth()

        return list;
    }
}
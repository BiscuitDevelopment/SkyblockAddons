package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ModelEndermanTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.model.ModelEnderman}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.ModelEnderman.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {

        // Objective: Add:
        //
        // @Override
        // public render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
        //     ModelEndermanHook.setEndermanColor();
        //     super.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
        // }

        MethodNode updateScreen = TransformerMethod.render.createMethodNode();
        updateScreen.instructions.add(render());
        classNode.methods.add(updateScreen);
    }

    private InsnList render() {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/ModelEndermanHook", "setEndermanColor",
                "()V", false)); // ModelEndermanHook.setEndermanColor();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        for (int var = 2; var <= 7; var++) { // Load all 6 float parameters
            list.add(new VarInsnNode(Opcodes.FLOAD, var));
        }
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TransformerClass.ModelBiped.getNameRaw(), TransformerMethod.render.getName(),
                TransformerMethod.render.getDescription(), false)); // super.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }
}

package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class RenderEndermanTransformer implements ITransformer {
    /**
     * {@link net.minecraft.client.renderer.entity.RenderEnderman}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.RenderEnderman.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.getEntityTexture_RenderEnderman.matches(methodNode)) {

                // Objective:
                // Find: return endermanTextures;
                // Change to: return RenderEndermanHook.getEndermanTexture(endermanTextures);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.ARETURN) {
                        methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/RenderEndermanHook",
                                "getEndermanTexture", "("+TransformerClass.ResourceLocation.getName()+")"+TransformerClass.ResourceLocation.getName(), false));
                        break;
                    }
                }
            }
        }
    }
}
package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class TileEntityEnderChestRendererTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.TileEntityEnderChestRenderer.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.renderTileEntityAt.matches(methodNode)) {

                // Objective:
                // Find: this.bindTexture(ENDER_CHEST_TEXTURE);
                // Replacement: TileEntityEnderChestRendererHook.bindTexture(this, (ResourceLocation)ENDER_CHEST_TEXTURE);

                // Objective:
                // Find: this.field_147521_c.renderAll();
                // Insert 2 lines before: TileEntityEnderChestRendererHook.setEnderchestColor();

                int bindTextureCount = 0;

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();

                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.TileEntityEnderChestRenderer.getNameRaw())
                                && TransformerMethod.bindTexture.matches(methodInsnNode)) { // TileEntityEnderChestRendererHook.bindTexture(ENDER_CHEST_TEXTURE);
                            if (bindTextureCount == 1) { // Find the second statement, not the first one.
                                methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/TileEntityEnderChestRendererHook",
                                        // Add TileEntityEnderChestRendererHook.bindTexture(this, (ResourceLocation)ENDER_CHEST_TEXTURE);
                                        "bindTexture", "("+TransformerClass.TileEntityEnderChestRenderer.getName()+TransformerClass.ResourceLocation.getName()+")V", false));
                                iterator.remove(); // Remove the old method call.
                            }
                            bindTextureCount++;
                        } else if (methodInsnNode.owner.equals(TransformerClass.ModelChest.getNameRaw())
                                && TransformerMethod.renderAll.matches(methodInsnNode)) { // The two lines are to make sure its before the "this" & the "field_147521_c".
                            methodNode.instructions.insertBefore(methodNode.instructions.get(methodNode.instructions.indexOf(abstractNode)-2), insertChangeEnderchestColor());
                        }
                    }
                }
                break;
            }
        }
    }

    private InsnList insertChangeEnderchestColor() {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/TileEntityEnderChestRendererHook", "setEnderchestColor",
                "()V", false)); // TileEntityEnderChestRendererHook.setEnderchestColor();

        return list;
    }
}

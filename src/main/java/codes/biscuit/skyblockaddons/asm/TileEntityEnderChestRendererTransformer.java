package codes.biscuit.skyblockaddons.asm;

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
        return new String[]{"net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode); // Map all of the method names.
            if (nameMatches(methodName,"renderTileEntityAt", "func_180535_a")) {

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
                        if (nameMatches(methodInsnNode.owner, "net/minecraft/client/renderer/tileentity/TileEntityEnderChestRenderer")
                                && nameMatches(methodInsnNode.name, "bindTexture", "func_147499_a")) { // TileEntityEnderChestRendererHook.bindTexture(ENDER_CHEST_TEXTURE);
                            if (bindTextureCount == 1) { // Find the second statement, not the first one.
                                methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/TileEntityEnderChestRendererHook",
                                        // Add TileEntityEnderChestRendererHook.bindTexture(this, (ResourceLocation)ENDER_CHEST_TEXTURE);
                                        "bindTexture", "(Lnet/minecraft/client/renderer/tileentity/TileEntityEnderChestRenderer;Lnet/minecraft/util/ResourceLocation;)V", false));
                                iterator.remove(); // Remove the old method call.
                            }
                            bindTextureCount++;
                        } else if (nameMatches(methodInsnNode.owner, "net/minecraft/client/model/ModelChest") &&
                                nameMatches(methodInsnNode.name, "renderAll", "func_78231_a")) { // The two lines are to make sure its before the "this" & the "field_147521_c".
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

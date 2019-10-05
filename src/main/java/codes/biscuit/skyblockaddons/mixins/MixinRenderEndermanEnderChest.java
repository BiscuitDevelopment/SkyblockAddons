package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(RenderEnderman.class)
public abstract class MixinRenderEndermanEnderChest {

	@Shadow
	abstract ModelEnderman getMainModel();

	@Inject(
			method = "getEntityTexture",
			at = @At(
					value = "HEAD"
			)
	)
	private void getEntityTexture(EntityEnderman entity) {
		SkyblockAddons main = SkyblockAddons.getInstance();

		if (main.getUtils().isOnSkyblock()) {
			if (this.getMainModel().isCarrying) {
				// Fixes Ender Chest rendering on 1.12.x clients
				ItemStack enderChestItemStack = new ItemStack(Blocks.ENDER_CHEST);
				IBlockState enderChestBlockState = Block.getBlockFromItem(enderChestItemStack.getItem()).getBlockState().getBaseState();
				entity.setHeldBlockState(enderChestBlockState);
			}
		}
	}

}
package codes.biscuit.skyblockaddons.mixins;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.List;

@Mixin(RenderLivingBase.class)
public class MixinRenderLivingEntity {

	// net.minecraft.client.renderer.entity.Render - L374 - deadmau5

	private boolean isCoolPerson;

	@Redirect(method = "applyRotations", at = @At(value = "INVOKE",
			target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0))
	private boolean applyRotationsEquals(String name, Object obj) {
		List<String> coolPeople = Arrays.asList("Dinnerbone", "Biscut", "CraftedFury", "GoldenDusk");
		return (isCoolPerson = coolPeople.contains(obj));
	}

	@Redirect(method = "applyRotations", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/entity/player/EntityPlayer;isWearing(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z", ordinal = 0))
	private boolean applyRotationsIsWearing(EntityPlayer entityPlayer, EnumPlayerModelParts parts) {
		return (isCoolPerson && !entityPlayer.isWearing(parts));
	}

}
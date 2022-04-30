package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelEnderman.class)
public class MixinModelEnderman extends ModelBiped {

    @Override
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getUtils().getLocation() == Location.DRAGONS_NEST && main.getConfigValues().isEnabled(Feature.CHANGE_ZEALOT_COLOR)) {
            int color = main.getConfigValues().getColor(Feature.CHANGE_ZEALOT_COLOR);
            ColorUtils.bindColor(color);
        }
        super.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
    }
}

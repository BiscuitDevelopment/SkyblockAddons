package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonPlayer;
import codes.biscuit.skyblockaddons.features.EntityOutlines.EntityOutlineRenderer;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;

import java.util.Set;

// cough nothing to see here
public class RendererLivingEntityHook {

    // TODO: Convert this to UUIDs instead of names
    // no don't ask to be added lol, for now these are just like my admins
    private static final Set<String> coolPeople = Sets.newHashSet("Dinnerbone", "Biscut", "Pinpointed", "Berded", "Potat_owo", "Pnda__", "Throwpo", "StopUsingSBE");
    private static boolean isCoolPerson;

    public static boolean equals(String string, Object otherString) {
        isCoolPerson = coolPeople.contains(string);
        return isCoolPerson;
    }

    public static boolean isWearing(EntityPlayer entityPlayer, EnumPlayerModelParts p_175148_1_) {
        return (!isCoolPerson && entityPlayer.isWearing(p_175148_1_)) ||
                (isCoolPerson && !entityPlayer.isWearing(p_175148_1_));
    }

    public static int setOutlineColor(EntityLivingBase entity, int originalColor) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES) &&
                main.getUtils().isInDungeon() && main.getDungeonManager().getTeammates().containsKey(entity.getName())) {
            DungeonPlayer dungeonPlayer = main.getDungeonManager().getTeammates().get(entity.getName());

            if (dungeonPlayer.isCritical()) {
                return Minecraft.getMinecraft().fontRendererObj.getColorCode('c');
            } else if (dungeonPlayer.isLow()) {
                return Minecraft.getMinecraft().fontRendererObj.getColorCode('e');
            }
        } else {
            Integer i = EntityOutlineRenderer.getCustomOutlineColor(entity);
            if (i != null) {
                return i;
            }
        }
        return originalColor;
    }
}

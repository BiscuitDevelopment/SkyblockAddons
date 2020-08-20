package codes.biscuit.skyblockaddons.asm.hooks;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;

import java.util.Set;

// cough nothing to see here
public class RendererLivingEntityHook {

    // no don't ask to be added lol, for now these are just like my admins
    private static Set<String> coolPeople = Sets.newHashSet("Dinnerbone", "Biscut", "Pinpointed", "Berded", "Potat_owo");
    private static boolean isCoolPerson;

    public static boolean equals(String string, Object otherString) {
        isCoolPerson = coolPeople.contains(string);
        return isCoolPerson;
    }

    public static boolean isWearing(EntityPlayer entityPlayer, EnumPlayerModelParts p_175148_1_) {
        return (!isCoolPerson && entityPlayer.isWearing(p_175148_1_)) ||
                (isCoolPerson && !entityPlayer.isWearing(p_175148_1_));
    }
}

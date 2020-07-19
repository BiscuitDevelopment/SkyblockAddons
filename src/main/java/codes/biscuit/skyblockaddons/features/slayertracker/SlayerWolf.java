package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

public class SlayerWolf extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerWolf() {
        super(Feature.SLAYER_WOLF, "wolf");

        drops = new ArrayList<SlayerDrop>();
        {
            ItemStack stack = new ItemStack(Items.ghast_tear);
            stack.setStackDisplayName("Wolf Tooth");
            drops.add(new SlayerDrop(this, "WOLF_TOOTH", "WolfTeeth", ItemRarity.UNCOMMON, stack));
        }
        {
            ItemStack stack = new ItemStack(Blocks.trapdoor);
            stack.setStackDisplayName("Hamster Wheel");
            drops.add(new SlayerDrop(this, "HAMSTER_WHEEL", "HamsterWheels", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "9e0afa8f-22b3-38d3-b5eb-b5328a73d963");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value: \"eyJ0aW1lc3RhbXAiOjE1Njk2MTMzNzQyMDQsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M3MzhiOGFmOGQ3Y2UxYTI2ZGM2ZDQwMTgwYjM1ODk0MDNlMTFlZjM2YTY2ZDdjNDU5MDAzNzczMjgyOTU0MmUifX19\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Spirit Rune");
            drops.add(new SlayerDrop(this, "RUNE:SPIRIT", "SpiritRunes", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.enchanted_book);
            stack.setStackDisplayName("Enchanted Book");
            drops.add(new SlayerDrop(this, "ENCHANTED_BOOK", "Crit6Books", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.fish, 1, 1);
            stack.setStackDisplayName("Grizzly Bait");
            drops.add(new SlayerDrop(this, "GRIZZLY_BAIT", "GrizzlyBaits", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.spawn_egg, 1 , 96);
            stack.setStackDisplayName("Red Claw Egg");
            drops.add(new SlayerDrop(this, "RED_CLAW_EGG", "RedClawEggs", ItemRarity.EPIC, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.quartz);
            stack.setStackDisplayName("Overflux Capacitor");
            drops.add(new SlayerDrop(this, "OVERFLUX_CAPACITOR", "Overfluxes", ItemRarity.EPIC, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "0120ccd2-5ee3-314a-b771-35128cc17d22");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value:\"eyJ0aW1lc3RhbXAiOjE1Njk4NDQxMTA1MTEsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzczNGZiMzIwMzIzM2VmYmFlODI2MjhiZDRmY2E3MzQ4Y2QwNzFlNWI3YjUyNDA3ZjFkMWQyNzk0ZTMxNzk5ZmYifX19\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Couture Rune");
            drops.add(new SlayerDrop(this, "RUNE:COUTURE", "CoutureRunes", ItemRarity.LEGENDARY, stack));
        }
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

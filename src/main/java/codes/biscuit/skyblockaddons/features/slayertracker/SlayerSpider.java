package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

public class SlayerSpider extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerSpider() {
        super(Feature.SLAYER_SPIDER, "spider");

        drops = new ArrayList<SlayerDrop>();
        {
            ItemStack stack = new ItemStack(Items.string);
            stack.setStackDisplayName("Tarantula Web");
            drops.add(new SlayerDrop(this, "TARANTULA_WEB", "TarantulaWebs", ItemRarity.UNCOMMON, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.dye, 1, 10);
            stack.setStackDisplayName("Toxic Arrow Poison");
            drops.add(new SlayerDrop(this, "TOXIC_ARROW_POISON", "ToxicArrowPoisons", ItemRarity.UNCOMMON, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "3fe28c63-f3fc-30c2-8e74-ff1297977213");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgzYjMwZTlkMTM1YjA1MTkwZWVhMmMzYWM2MWUyYWI1NWEyZDgxZTFhNThkYmIyNjk4M2ExNDA4MjY2NCJ9fX0=\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Bite Rune");
            drops.add(new SlayerDrop(this, "SPIDER_CATALYST", "SpiderCatalysts", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.enchanted_book);
            stack.setStackDisplayName("Enchanted Book");
            drops.add(new SlayerDrop(this, "ENCHANTED_BOOK", "BaneOfArthropod6Books", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "d4f365d2-e20c-366c-8cba-653d52ace982");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value:\"eyJ0aW1lc3RhbXAiOjE1NjkzMjYxMjU5NTQsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQzYTFhZDRmY2M0MmZiNjNjNjgxMzI4ZTQyZDYzYzgzY2ExOTNiMzMzYWYyYTQyNjcyOGEyNWE4Y2M2MDA2OTIifX19\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Bite Rune");
            drops.add(new SlayerDrop(this, "RUNE:BITE", "BiteRunes", ItemRarity.EPIC, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.golden_shovel);
            stack.setStackDisplayName("Fly Swatter");
            stack.addEnchantment(Enchantment.protection, 0);
            drops.add(new SlayerDrop(this, "FLY_SWATTER", "FlySwatters", ItemRarity.EPIC, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "c89b16d8-4122-31e0-bb59-15cc95cdfe2c");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDQyY2Y4Y2U0ODdiNzhmYTIwM2Q1NmNmMDE0OTE0MzRiNGMzM2U1ZDIzNjgwMmM2ZDY5MTQ2YTUxNDM1YjAzZCJ9fX0\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Tarantula Talisman");
            drops.add(new SlayerDrop(this, "TARANTULA_TALISMAN", "TarantulaTalismans", ItemRarity.EPIC, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.rotten_flesh);
            stack.setStackDisplayName("Digested Mosquito");
            drops.add(new SlayerDrop(this, "DIGESTED_MOSQUITO", "DigestedMosquitoes", ItemRarity.LEGENDARY, stack));
        }
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

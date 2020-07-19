package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

public class SlayerZombie extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerZombie() {
        super(Feature.SLAYER_ZOMBIE, "zombie");

        drops = new ArrayList<SlayerDrop>();
        {
            ItemStack stack = new ItemStack(Items.rotten_flesh);
            stack.setStackDisplayName("Revenant Flesh");
            drops.add(new SlayerDrop(this, "REVENANT_FLESH", "RevenantFlesh", ItemRarity.UNCOMMON, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.coal, 1 ,1);
            stack.setStackDisplayName("Foul Flesh");
            drops.add(new SlayerDrop(this, "FOUL_FLESH", "FoulFlesh", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "3b525679-ed3f-31dd-8972-0fd6dd964759");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value:\"eyJ0aW1lc3RhbXAiOjE1Njg0NTUxMzM1NTcsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E4YzQ4MTEzOTVmYmY3ZjYyMGYwNWNjMzE3NWNlZjE1MTVhYWY3NzViYTA0YTAxMDQ1MDI3ZjA2OTNhOTAxNDcifX19\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Pestilence Rune");
            drops.add(new SlayerDrop(this, "RUNE:ZOMBIE_SLAYER", "PestilenceRunes", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "5bf9191d-dcdf-3c0f-aff9-7fcc5cb0a001");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA2MjUzNjliMGE3YjA1MjYzMmRiNmI5MjZhODc2NzAyMTk1Mzk5MjI4MzZhYzU5NDBiZTI2ZDM0YmYxNGUxMCJ9fX0\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Undead Catalyst");
            drops.add(new SlayerDrop(this, "UNDEAD_CATALYST", "UndeadCatalysts", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.enchanted_book);
            stack.setStackDisplayName("Enchanted Book");
            drops.add(new SlayerDrop(this, "ENCHANTED_BOOK", "Smite6Books", ItemRarity.RARE, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "0862e0b0-a14f-3f93-894f-013502936b59");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value:\"eyJ0aW1lc3RhbXAiOjE1Njg0NTc0MjAxMzcsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RiYWQ5OWVkM2M4MjBiNzk3ODE5MGFkMDhhOTM0YTY4ZGZhOTBkOTk4NjgyNWRhMWM5N2Y2ZjIxZjQ5YWQ2MjYifX19\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Beheaded Horror");
            drops.add(new SlayerDrop(this, "BEHEADED_HORROR", "BeheadedHorrors", ItemRarity.EPIC, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "5ace63c5-b3c9-306a-887c-16db7efea0f0");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value:\"eyJ0aW1lc3RhbXAiOjE1NjgzNjYzMjYwNzEsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I4OGNmYWZhNWYwM2Y4YWVmMDQyYTE0Mzc5OWU5NjQzNDJkZjc2YjdjMWViNDYxZjYxOGUzOThmODRhOTlhNjMifX19\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Revenant Catalyst");
            drops.add(new SlayerDrop(this, "REVENANT_CATALYST", "RevenantCatalysts", ItemRarity.EPIC, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.skull, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", "b8480d6d-7769-33ea-8dba-3cb5a01a69c0");
            try {
                tag.setTag("Properties", JsonToNBT.getTagFromJson("{textures: [{Value:\"eyJ0aW1lc3RhbXAiOjE1Njg0NjEwMDY4NjcsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJjNGE2NWM2ODliMmQzNjQwOTEwMGE2MGMyYWI4ZDNkMGE2N2NlOTRlZWEzYzFmN2FjOTc0ZmQ4OTM1NjhiNWQifX19\"}]}"));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            stack.setTagInfo("SkullOwner", tag);
            stack.setStackDisplayName("Snake Rune");
            drops.add(new SlayerDrop(this, "RUNE:SNAKE", "SnakeRunes", ItemRarity.LEGENDARY, stack));
        }
        {
            ItemStack stack = new ItemStack(Items.diamond);
            stack.setStackDisplayName("Scythe Blade");
            stack.addEnchantment(Enchantment.protection, 0);
            drops.add(new SlayerDrop(this, "SCYTHE_BLADE", "ScytheBlades", ItemRarity.LEGENDARY, stack));
        }
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

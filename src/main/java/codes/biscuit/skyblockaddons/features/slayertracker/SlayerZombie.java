package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;

import java.util.ArrayList;

public class SlayerZombie extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerZombie() {
        super(Feature.SLAYER_ZOMBIE, "zombie");

        drops = new ArrayList<SlayerDrop>();
        drops.add(new SlayerDrop(this, "REVENANT_FLESH", "RevenantFlesh", ItemRarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "FOUL_FLESH", "FoulFlesh", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "RUNE:ZOMBIE_SLAYER", "PestilenceRunes", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "UNDEAD_CATALYST", "UndeadCatalysts", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "ENCHANTED_BOOK", "Smite6Books", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "BEHEADED_HORROR", "BeheadedHorrors", ItemRarity.EPIC));
        drops.add(new SlayerDrop(this, "REVENANT_CATALYST", "RevenantCatalysts", ItemRarity.EPIC));
        drops.add(new SlayerDrop(this, "RUNE:SNAKE", "SnakeRunes", ItemRarity.LEGENDARY));
        drops.add(new SlayerDrop(this, "SCYTHE_BLADE", "ScytheBlades", ItemRarity.LEGENDARY));
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

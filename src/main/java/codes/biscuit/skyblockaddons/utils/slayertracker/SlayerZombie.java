package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;

import java.util.ArrayList;

public class SlayerZombie extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerZombie() {
        super(Feature.SLAYER_ZOMBIE, "zombie");

        drops = new ArrayList<SlayerDrop>();
        drops.add(new SlayerDrop(this, "REVENANT_FLESH", "RevenantFlesh", Rarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "FOUL_FLESH", "FoulFlesh", Rarity.RARE));
        drops.add(new SlayerDrop(this, "RUNE:ZOMBIE_SLAYER", "PestilenceRunes", Rarity.RARE));
        drops.add(new SlayerDrop(this, "UNDEAD_CATALYST", "UndeadCatalysts", Rarity.RARE));
        drops.add(new SlayerDrop(this, "ENCHANTED_BOOK", "Smite6Books", Rarity.RARE));
        drops.add(new SlayerDrop(this, "BEHEADED_HORROR", "BeheadedHorrors", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "REVENANT_CATALYST", "RevenantCatalysts", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "RUNE:SNAKE", "SnakeRunes", Rarity.LEGENDARY));
        drops.add(new SlayerDrop(this, "SCYTHE_BLADE", "ScytheBlades", Rarity.LEGENDARY));
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

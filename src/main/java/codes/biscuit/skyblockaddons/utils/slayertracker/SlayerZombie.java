package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;

import java.util.ArrayList;

public class SlayerZombie extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerZombie() {
        super(Feature.SLAYER_ZOMBIE, "zombie");

        drops = new ArrayList<SlayerDrop>();
        drops.add(new SlayerDrop(this, "Revenant Flesh", "slayerDropRevenantFlesh", Rarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "Foul Flesh", "slayerDropFoulFlesh", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Pestilence Rune", "slayerDropPestilenceRunes", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Undead Catalyst", "slayerDropUndeadCatalysts", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Enchanted Book", "slayerDropSmite6Books", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Beheaded Horror", "slayerDropBeheadedHorrors", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "Revenant Catalyst", "slayerDropRevenantCatalysts", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "Snake Rune", "slayerDropSnakeRunes", Rarity.LEGENDARY));
        drops.add(new SlayerDrop(this, "Scythe Blade", "slayerDropScytheBlades", Rarity.LEGENDARY));
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

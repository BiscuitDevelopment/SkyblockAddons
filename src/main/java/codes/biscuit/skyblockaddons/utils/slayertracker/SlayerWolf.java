package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;

import java.util.ArrayList;

public class SlayerWolf extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerWolf() {
        super(Feature.SLAYER_WOLF, "wolf");

        drops = new ArrayList<SlayerDrop>();
        drops.add(new SlayerDrop(this, "Wolf Tooth", "slayerDropWolfTeeth", Rarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "Hamster Wheel", "slayerDropHamsterWheels", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Spirit Rune", "slayerDropSpiritRunes", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Critical VI Book", "slayerDropCrit6Books", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Grizzly Bait", "slayerDropGrizzlyBaits", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Red Claw Egg", "slayerDropRedClawEggs", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "Overflux Capacitor", "slayerDropOverfluxes", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "Couture Rune", "slayerDropCoutureRunes", Rarity.LEGENDARY));
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

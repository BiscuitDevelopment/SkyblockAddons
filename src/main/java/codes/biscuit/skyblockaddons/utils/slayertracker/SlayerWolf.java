package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;

import java.util.ArrayList;

public class SlayerWolf extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerWolf() {
        super(Feature.SLAYER_WOLF, "wolf");

        drops = new ArrayList<SlayerDrop>();
        drops.add(new SlayerDrop(this, "WOLF_TOOTH", "WolfTeeth", ItemRarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "HAMSTER_WHEEL", "HamsterWheels", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "RUNE:SPIRIT", "SpiritRunes", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "ENCHANTED_BOOK", "Crit6Books", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "GRIZZLY_BAIT", "GrizzlyBaits", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "RED_CLAW_EGG", "RedClawEggs", ItemRarity.EPIC));
        drops.add(new SlayerDrop(this, "OVERFLUX_CAPACITOR", "Overfluxes", ItemRarity.EPIC));
        drops.add(new SlayerDrop(this, "RUNE:COUTURE", "CoutureRunes", ItemRarity.LEGENDARY));
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

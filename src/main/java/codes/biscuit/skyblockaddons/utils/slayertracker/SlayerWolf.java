package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;

import java.util.ArrayList;

public class SlayerWolf extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerWolf() {
        super(Feature.SLAYER_WOLF, "wolf");

        drops = new ArrayList<SlayerDrop>();
        drops.add(new SlayerDrop(this, "WOLF_TOOTH", "WolfTeeth", Rarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "HAMSTER_WHEEL", "HamsterWheels", Rarity.RARE));
        drops.add(new SlayerDrop(this, "RUNE:SPIRIT", "SpiritRunes", Rarity.RARE));
        drops.add(new SlayerDrop(this, "ENCHANTED_BOOK", "Crit6Books", Rarity.RARE));
        drops.add(new SlayerDrop(this, "GRIZZLY_BAIT", "GrizzlyBaits", Rarity.RARE));
        drops.add(new SlayerDrop(this, "RED_CLAW_EGG", "RedClawEggs", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "OVERFLUX_CAPACITOR", "Overfluxes", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "RUNE:COUTURE", "CoutureRunes", Rarity.LEGENDARY));
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

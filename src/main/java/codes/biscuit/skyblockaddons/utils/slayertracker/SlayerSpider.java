package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;

import java.util.ArrayList;

public class SlayerSpider extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerSpider() {
        super(Feature.SLAYER_SPIDER, "spider");

        drops = new ArrayList<SlayerDrop>();
        drops.add(new SlayerDrop(this, "Tarantula Web", "slayerDropTarantulaWebs", Rarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "Toxic Arrow Poison", "slayerDropToxicArrowPoisons", Rarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "Spider Catalyst", "slayerDropSpiderCatalysts", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Enchanted Book", "slayerDropBaneOfArthropod6Books", Rarity.RARE));
        drops.add(new SlayerDrop(this, "Bite Rune", "slayerDropBiteRunes", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "Fly Swatter", "slayerDropFlySwatters", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "Tarantula Talisman", "slayerDropTarantulaTalismans", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "Digested Mosquito", "slayerDropDigestedMosquitoes", Rarity.LEGENDARY));
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

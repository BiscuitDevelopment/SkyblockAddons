package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;

import java.util.ArrayList;

public class SlayerSpider extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerSpider() {
        super(Feature.SLAYER_SPIDER, "spider");

        drops = new ArrayList<SlayerDrop>();
        drops.add(new SlayerDrop(this, "TARANTULA_WEB", "TarantulaWebs", ItemRarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "TOXIC_ARROW_POISON", "ToxicArrowPoisons", ItemRarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "SPIDER_CATALYST", "SpiderCatalysts", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "ENCHANTED_BOOK", "BaneOfArthropod6Books", ItemRarity.RARE));
        drops.add(new SlayerDrop(this, "RUNE:BITE", "BiteRunes", ItemRarity.EPIC));
        drops.add(new SlayerDrop(this, "FLY_SWATTER", "FlySwatters", ItemRarity.EPIC));
        drops.add(new SlayerDrop(this, "TARANTULA_TALISMAN", "TarantulaTalismans", ItemRarity.EPIC));
        drops.add(new SlayerDrop(this, "DIGESTED_MOSQUITO", "DigestedMosquitoes", ItemRarity.LEGENDARY));
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

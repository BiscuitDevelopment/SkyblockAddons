package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;

import java.util.ArrayList;

public class SlayerSpider extends SlayerBoss {

    ArrayList<SlayerDrop> drops;

    public SlayerSpider() {
        super(Feature.SLAYER_SPIDER, "spider");

        drops = new ArrayList<SlayerDrop>();
        drops.add(new SlayerDrop(this, "TARANTULA_WEB", "TarantulaWebs", Rarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "TOXIC_ARROW_POISON", "ToxicArrowPoisons", Rarity.UNCOMMON));
        drops.add(new SlayerDrop(this, "SPIDER_CATALYST", "SpiderCatalysts", Rarity.RARE));
        drops.add(new SlayerDrop(this, "ENCHANTED_BOOK", "BaneOfArthropod6Books", Rarity.RARE));
        drops.add(new SlayerDrop(this, "RUNE:BITE", "BiteRunes", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "FLY_SWATTER", "FlySwatters", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "TARANTULA_TALISMAN", "TarantulaTalismans", Rarity.EPIC));
        drops.add(new SlayerDrop(this, "DIGESTED_MOSQUITO", "DigestedMosquitoes", Rarity.LEGENDARY));
    }

    @Override
    public ArrayList<SlayerDrop> getDrops() {
        return drops;
    }
}

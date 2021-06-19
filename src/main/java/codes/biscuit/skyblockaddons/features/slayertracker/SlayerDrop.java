package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import com.google.common.base.CaseFormat;
import lombok.Getter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public enum SlayerDrop {

    // Revenant Drops
    REVENANT_FLESH(Items.rotten_flesh, "Revenant Flesh", "REVENANT_FLESH",ItemRarity.UNCOMMON, true),
    FOUL_FLESH(Items.coal, 1, "Foul Flesh", "FOUL_FLESH", ItemRarity.RARE),
    PESTILENCE_RUNE("Pestilence Rune", "RUNE", "ZOMBIE_SLAYER", ItemRarity.RARE, "3b525679-ed3f-31dd-8972-0fd6dd964759", "a8c4811395fbf7f620f05cc3175cef1515aaf775ba04a01045027f0693a90147"),
    UNDEAD_CATALYST("Undead Catalyst", "UNDEAD_CATALYST", ItemRarity.RARE, "5bf9191d-dcdf-3c0f-aff9-7fcc5cb0a001", "80625369b0a7b052632db6b926a87670219539922836ac5940be26d34bf14e10"),
    SMITE_SIX("Enchanted Book", "ENCHANTED_BOOK", ItemRarity.RARE, "smite", 6),
    BEHEADED_HORROR("Beheaded Horror", "BEHEADED_HORROR", ItemRarity.EPIC, "0862e0b0-a14f-3f93-894f-013502936b59", "dbad99ed3c820b7978190ad08a934a68dfa90d9986825da1c97f6f21f49ad626"),
    REVENANT_CATALYST("Revenant Catalyst", "REVENANT_CATALYST", ItemRarity.EPIC, "5ace63c5-b3c9-306a-887c-16db7efea0f0", "b88cfafa5f03f8aef042a143799e964342df76b7c1eb461f618e398f84a99a63"),
    SNAKE_RUNE("Snake Rune", "RUNE", "SNAKE", ItemRarity.LEGENDARY, "b8480d6d-7769-33ea-8dba-3cb5a01a69c0", "2c4a65c689b2d36409100a60c2ab8d3d0a67ce94eea3c1f7ac974fd893568b5d"),
    SCYTHE_BLADE(Items.diamond, "Scythe Blade", "SCYTHE_BLADE", ItemRarity.LEGENDARY, true),
    SHARD_OF_SHREDDED("Shard of the Shredded", "SHARD_OF_THE_SHREDDED", ItemRarity.LEGENDARY, "9ddf6967-40de-3534-903f-4d5d9c933d55", "70c5cc728c869ecf3c6e0979e8aa09c10147ed770417e4ba541aac382f0"),
    WARDEN_HEART("Warden Heart", "WARDEN_HEART", ItemRarity.LEGENDARY, "7adc7613-256a-3593-899b-d4d9bbf50387", "d45f4d139c9e89262ec06b27aaad73fa488ab49290d2ccd685a2554725373c9b"),

    // Tarantula Drops
    TARANTULA_WEB(Items.string, "Tarantula Web", "TARANTULA_WEB", ItemRarity.UNCOMMON, true),
    TOXIC_ARROW_POISON(Items.dye, 10, "Toxic Arrow Poison", "TOXIC_ARROW_POISON", ItemRarity.RARE),
    SPIDER_CATALYST("Spider Catalyst", "SPIDER_CATALYST", ItemRarity.RARE, "3fe28c63-f3fc-30c2-8e74-ff1297977213", "983b30e9d135b05190eea2c3ac61e2ab55a2d81e1a58dbb26983a14082664"),
    BANE_OF_ARTHROPODS_SIX("Enchanted Book", "ENCHANTED_BOOK", ItemRarity.RARE, "bane_of_arthropods", 6),
    BITE_RUNE("Bite Rune", "RUNE", "BITE", ItemRarity.EPIC, "d4f365d2-e20c-366c-8cba-653d52ace982", "43a1ad4fcc42fb63c681328e42d63c83ca193b333af2a426728a25a8cc600692"),
    FLY_SWATTER(Items.golden_shovel, "Fly Swatter", "FLY_SWATTER", ItemRarity.EPIC, true),
    TARANTULA_TALISMAN("Tarantula Talisman", "TARANTULA_TALISMAN", ItemRarity.EPIC, "c89b16d8-4122-31e0-bb59-15cc95cdfe2c", "442cf8ce487b78fa203d56cf01491434b4c33e5d236802c6d69146a51435b03d"),
    DIGESTED_MOSQUITO(Items.rotten_flesh, "Digested Mosquito", "DIGESTED_MOSQUITO", ItemRarity.LEGENDARY),

    // Sven Drops
    WOLF_TOOTH(Items.ghast_tear, "Wolf Tooth", "WOLF_TOOTH", ItemRarity.UNCOMMON, true),
    HAMSTER_WHEEL(Item.getItemFromBlock(Blocks.trapdoor), "Hamster Wheel", "HAMSTER_WHEEL", ItemRarity.RARE, true),
    SPIRIT_RUNE("Spirit Rune", "RUNE", "SPIRIT", ItemRarity.RARE, "9e0afa8f-22b3-38d3-b5eb-b5328a73d963", "c738b8af8d7ce1a26dc6d40180b3589403e11ef36a66d7c4590037732829542e"),
    CRITICAL_SIX("Enchanted Book", "ENCHANTED_BOOK", ItemRarity.RARE, "critical", 6),
    GRIZZLY_BAIT(Items.fish, 1, "Grizzly Bait", "GRIZZLY_BAIT", ItemRarity.RARE),
    RED_CLAW_EGG(Items.spawn_egg, 96, "Red Claw Egg", "RED_CLAW_EGG", ItemRarity.EPIC),
    OVERFLUX_CAPACITOR(Items.quartz, "Overflux Capacitor", "OVERFLUX_CAPACITOR", ItemRarity.EPIC),
    COUTURE_RUNE("Couture Rune", "RUNE", "COUTURE", ItemRarity.LEGENDARY, "0120ccd2-5ee3-314a-b771-35128cc17d22", "734fb3203233efbae82628bd4fca7348cd071e5b7b52407f1d1d2794e31799ff"),

    // Enderman Drops
    NULL_SPHERE(Items.firework_charge, "Null Sphere", "NULL_SPHERE", ItemRarity.UNCOMMON, true),
    TWILIGHT_ARROW_POISON(Items.dye, 5, "Twilight Arrow Poison", "TWILIGHT_ARROW_POISON", ItemRarity.UNCOMMON),
    ENDERSNAKE_RUNE("Endersnake Rune", "RUNE", "ENDERSNAKE", ItemRarity.LEGENDARY, "9de9bfa4-30c2-383c-8a67-44c7ba4206ab", "c3a9acbb7d3d49b1d54d26111104d0da57d8b4ab37885b4bbd240ac71074cad2"),
    SUMMONING_EYE("Summoning Eye", "SUMMONING_EYE", ItemRarity.EPIC, "00a702b9-7bad-3205-a04b-52478d8c0e7f", "daa8fc8de6417b48d48c80b443cf5326e3d9da4dbe9b25fcd49549d96168fc0"),
    MANA_STEAL_ONE("Enchanted Book", "ENCHANTED_BOOK", ItemRarity.COMMON, "mana_steal", 1),
    TRANSMISSION_TUNER("Transmission Tuner", "TRANSMISSION_TUNER", ItemRarity.EPIC, "df5671b6-329a-3fd7-9f56-71b6329a9fd7", "8ae54d03ce05106f6f745b8f851344ec38e68dd3307a31c843b08212df546dd9"),
    NULL_ATOM(Item.getItemFromBlock(Blocks.wooden_button), "Null Atom", "NULL_ATOM", ItemRarity.RARE, true),
    POCKET_ESPRESSO_MACHINE("Pocket Espresso Machine", "POCKET_ESPRESSO_MACHINE", ItemRarity.COMMON, "9bf867c8-d5b6-33e3-8fa3-f4a573979ebe", "666070ce03a545ee4d263bcf27f36338d249d7cb7a2376f92c1673ae134e04b6"),
    SMARTY_PANTS_ONE("Enchanted Book", "ENCHANTED_BOOK", ItemRarity.COMMON, "smarty_pants", 1),
    END_RUNE("End Rune", "RUNE", "ENDERSNAKE", ItemRarity.LEGENDARY, "9d07e315-87f9-31e7-98a2-0372ebd8e660", "3b11fb90db7f57beb435954013b1c7ef776c6bd96cbf3308aa8ebac29591ebbd"),
    HANDY_BLOOD_CHALICE("Handy Blood Chalice", "HANDY_BLOOD_CHALICE", ItemRarity.COMMON, "d17ab030-ec6c-3a88-9805-50b5812690fb", "431cd7ed4e4bf07c3dfd9ba498708e730e69d807335affabc12d87ff542f6a88"),
    SINFUL_DICE("Sinful Dice", "SINFUL_DICE", ItemRarity.EPIC, "05ab8a23-a718-3dbb-8307-d999ebed1e24", "6e22c298e7c6336af17909ac1f1ee6834b58b1a3cc99aba255ca7eaeb476173"),
    EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER("Exceedingly Rare Ender Artifact Upgrader", "EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER", ItemRarity.LEGENDARY, "eac161df-59c5-3647-92eb-950d53331e0e", "1259231a946987ea53141789a09496f098d6ecac412a01e0a24c906a99fdbd9a"),
    VOID_CONQUEROR_ENDERMAN_SKIN("Void Conqueror Enderman Skin", "VOID_CONQUEROR_ENDERMAN_SKIN", ItemRarity.LEGENDARY, "301afb75-07dd-37ce-94a1-7c5c40ab2512", "8fff41e1afc597b14f77b8e44e2a134dabe161a1526ade80e6290f2df331dc11"),
    ETHERWARP_MERGER("Etherwarp Merger", "ETHERWARP_MERGER", ItemRarity.EPIC, "209e7834-3376-36e1-84eb-da13ef083836", "3e5314f4919691ccbf807743dae47ae45ac2e3ff08f79eecdd452fe602eff7f6"),
    JUDGEMENT_CORE("Judgement Core", "JUDGEMENT_CORE", ItemRarity.LEGENDARY, "ed896594-8655-3212-933e-c67bca300084", "2f3ddd7f81089c85b26ed597675519f03a1dcd6d1713e0cfc66afb8743cbe0"),
    ENCHANT_RUNE("Enchant Rune", "RUNE", "ENCHANT", ItemRarity.LEGENDARY, "1a34ecd4-6a5f-35aa-b5d3-617be4684d9a", "59ffacec6ee5a23d9cb24a2fe9dc15b24488f5f71006924560bf12148421ae6d"),
    ENDER_SLAYER_SEVEN("Enchanted Book", "ENCHANTED_BOOK", ItemRarity.RARE, "ender_slayer", 7);

    @Getter
    private final String skyblockID;
    @Getter
    private final ItemRarity rarity;
    @Getter
    private final ItemStack itemStack;
    @Getter
    private String runeID;

    /**
     * Creates a slayer drop with an item, display name, skyblock id, and item rarity
     */
    SlayerDrop(Item item, String name, String skyblockID, ItemRarity rarity) {
        this(item, name, skyblockID, rarity, false);
    }

    /**
     * Creates an enchanted book slayer drop with a display name, skyblock id, item rarity,
     * skyblock enchant name, and enchant level.
     */
    SlayerDrop(String name, String skyblockID, ItemRarity rarity, String enchantID, int enchantLevel) {
        this.itemStack = ItemUtils.createEnchantedBook(name, skyblockID, enchantID, enchantLevel);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Creates a slayer drop with an item, display name, skyblock id, item rarity, and enchanted state
     */
    SlayerDrop(Item item, String name, String skyblockID, ItemRarity rarity, boolean enchanted) {
        this(item, 0, name, skyblockID, rarity, enchanted);
    }

    /**
     * Creates a slayer drop with an item, item meta, display name, skyblock id, and item rarity
     */
    SlayerDrop(Item item, int meta, String name, String skyblockID, ItemRarity rarity) {
        this(item, meta, name, skyblockID, rarity, false);
    }

    /**
     * Creates a slayer drop with an item, item meta, display name, skyblock id, item rarity, and enchanted state
     */
    SlayerDrop(Item item, int meta, String name, String skyblockID, ItemRarity rarity, boolean enchanted) {
        this.itemStack = ItemUtils.createItemStack(item, meta, name, skyblockID, enchanted);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Creates a player skull with a display name, skyblock id, item rarity, skull id, and skin texture link
     */
    SlayerDrop(String name, String skyblockID, ItemRarity rarity, String skullID, String textureURL) {
        this.itemStack = ItemUtils.createSkullItemStack(name, skyblockID, skullID, textureURL);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Creates a player skull with a display name, skyblock id, rune id, item rarity, skull id, and skin texture link
     */
    SlayerDrop(String name, String skyblockID, String runeID, ItemRarity rarity, String skullID, String textureURL) {
        this(name, skyblockID, rarity, skullID, textureURL);

        this.runeID = runeID;
    }

    private static final HashMap<String, String> internalItemTranslations = new HashMap<>();

    static {
        internalItemTranslations.put("revenantFlesh", "Revenant Flesh");
        internalItemTranslations.put("foulFlesh", "Foul Flesh");
        internalItemTranslations.put("pestilenceRune", "Pestilence Rune");
        internalItemTranslations.put("undeadCatalyst", "Undead Catalyst");
        internalItemTranslations.put("smiteSix", "Smite 6");
        internalItemTranslations.put("beheadedHorror", "Beheaded Horror");
        internalItemTranslations.put("revenantCatalyst", "Revenant Catalyst");
        internalItemTranslations.put("snakeRune", "Snake Rune");
        internalItemTranslations.put("scytheBlade", "Scythe Blade");
        internalItemTranslations.put("tarantulaWeb", "Tarantula Web");
        internalItemTranslations.put("toxicArrowPoison", "Toxic Arrow Poison");
        internalItemTranslations.put("spiderCatalyst", "Spider Catalyst");
        internalItemTranslations.put("baneOfArthropodsSix", "Bane Of Arthropods 6");
        internalItemTranslations.put("biteRune", "Bite Rune");
        internalItemTranslations.put("flySwatter", "Fly Swatter");
        internalItemTranslations.put("tarantulaTalisman", "Tarantula Talisman");
        internalItemTranslations.put("digestedMosquito", "Digested Mosquito");
        internalItemTranslations.put("wolfTooth", "Wolf Tooth");
        internalItemTranslations.put("hamsterWheel", "Hamster Wheel");
        internalItemTranslations.put("spiritRune", "Spirit Rune");
        internalItemTranslations.put("criticalSix", "Critical 6");
        internalItemTranslations.put("grizzlyBait", "Grizzly Bait");
        internalItemTranslations.put("redClawEgg", "Red Claw Egg");
        internalItemTranslations.put("overfluxCapacitor", "Overflux Capacitor");
        internalItemTranslations.put("coutureRune", "Couture Rune");
        internalItemTranslations.put("bossesKilled", "Bosses Killed");
        internalItemTranslations.put("nullSphere", "Null Sphere");
        internalItemTranslations.put("twilightArrowPoison", "Twilight Arrow Poison");
        internalItemTranslations.put("endersnakeRune", "Endersnake Rune");
        internalItemTranslations.put("summoningEye", "Summoning Eye");
        internalItemTranslations.put("manaStealOne", "Mana Steal 1");
        internalItemTranslations.put("transmissionTuner", "Transmission Tuner");
        internalItemTranslations.put("nullAtom", "Null Atom");
        internalItemTranslations.put("pocketEspressoMachine", "Pocket Espresso Machine");
        internalItemTranslations.put("smartyPantsOne", "Smarty Pants 1");
        internalItemTranslations.put("endRune", "End Rune");
        internalItemTranslations.put("handyBloodChalice", "Handy Blood Chalice");
        internalItemTranslations.put("sinfulDice", "Sinful Dice");
        internalItemTranslations.put("exceedinglyRareEnderArtifactUpgrader", "Exceedingly Rare Ender Artifact Upgrader");
        internalItemTranslations.put("voidConquerorEndermanSkin", "Void Conqueror Enderman Skin");
        internalItemTranslations.put("etherwarpMerger", "Etherwarp Merger");
        internalItemTranslations.put("judgementCore", "Judgement Core");
        internalItemTranslations.put("enchantRune", "Enchant Rune");
        internalItemTranslations.put("enderSlayerSeven", "Ender Slayer 7");
    }

    public String getDisplayName() {
        return internalItemTranslations.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name()));
    }
}

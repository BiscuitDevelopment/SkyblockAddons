package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import com.google.common.base.CaseFormat;
import lombok.Getter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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
    OVERFLUX_CAPACITOR(Items.quartz,"Overflux Capacitor", "OVERFLUX_CAPACITOR", ItemRarity.EPIC),
    COUTURE_RUNE("Couture Rune", "RUNE", "COUTURE", ItemRarity.LEGENDARY, "0120ccd2-5ee3-314a-b771-35128cc17d22", "734fb3203233efbae82628bd4fca7348cd071e5b7b52407f1d1d2794e31799ff"),
    ;

    @Getter private String skyblockID;
    @Getter private ItemRarity rarity;
    @Getter private ItemStack itemStack;
    @Getter private String runeID;

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

    public String getDisplayName() {
        return Translations.getMessage("slayerTracker." +  CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                this.name()));
    }
}

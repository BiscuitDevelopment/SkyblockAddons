package codes.biscuit.skyblockaddons.utils.bosstracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.ItemDiff;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.item.ItemUtils;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.slayertracker.SlayerTracker;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DragonBossTracker extends BossTracker {

    public static final String dragsRecent = "dragonsRecent", dragsSince = "dragonsSince";
    public static final String dragsSinceSupStr = "dragonsSinceSuperior";
    public static final String dragsSinceAOTDStr = "dragonsSinceAOTD";
    public static final String dragsSincePetStr = "dragonsSincePet";
    public static final String eyesPlacedStr = "eyesPlaced";
    public int eyePool = 0;
    public boolean myDrag = false;

    @Getter
    private ArrayList<Stat> dragsSinceStatList;

    @Getter
    private ArrayList<DragonType> recent;

    public DragonBossTracker() {
        super(Feature.DRAGON_STATS_TRACKER, "dragonStats");
        dragsSinceStatList = new ArrayList<>();
        dragsSinceStatList.add(new Stat(dragsSinceSupStr, Rarity.LEGENDARY));
        dragsSinceStatList.add(new Stat(dragsSinceAOTDStr, Rarity.LEGENDARY));
        dragsSinceStatList.add(new Stat(dragsSincePetStr, Rarity.LEGENDARY));
        dragsSinceStatList.add(new Stat(eyesPlacedStr, Rarity.EPIC));
        recent = new ArrayList<>();
    }

    public Stat getDragsSinceStat(String statName) {
        for (Stat stat : dragsSinceStatList)
            if (statName.equals(stat.getName())) return stat;
        return null;
    }

    public void dragonSpawned(String message) {
        if (eyePool > 0) {
            myDrag = true;

            for (DragonType type : DragonType.values())
                if (message.toLowerCase().contains(type.name.replaceFirst("dragon","")))
                {
                    recent.remove(0);
                    recent.add(type);
                    break;
                }

            if (message.toLowerCase().contains("superior"))
                getDragsSinceStat(dragsSinceSupStr).setCount(0);

            getDragsSinceStat(eyesPlacedStr).setCount(getDragsSinceStat(eyesPlacedStr).getCount() + eyePool);
            eyePool = 0;
        }
    }

    public void dragonKilled() {
        if (!myDrag) return;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, 30);
        stopAcceptingTimestamp = calendar.getTime();
        for (Stat stat : dragsSinceStatList) {
            if (stat == getDragsSinceStat(dragsSinceSupStr))
                continue;

            if (stat.getCount() < 0)
                stat.setCount(1);
            else
                stat.setCount(stat.getCount() + 1);
        }

        myDrag = false;
    }

    public void checkForDrops(List<ItemDiff> invDifference) {
        for (ItemDiff diff : invDifference) {
            String ID = ItemUtils.getSkyBlockItemID(diff.getExtraAttributes());
            switch (ID) {
                case "ASPECT_OF_THE_DRAGON":
                    getDragsSinceStat(dragsSinceAOTDStr).setCount(0);
                    break;
                case "PET":
                    if (ItemUtils.getPetInfo(diff.getExtraAttributes()).getType().equals("ENDER_DRAGON")) {
                        getDragsSinceStat(dragsSincePetStr).setCount(0);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void reset() {
        eyePool = 0;
        myDrag = false;
    }

    @Override
    public ArrayList<Stat> getStats() {
        return dragsSinceStatList;
    }

    public void LoadPersistentValues() {
        JsonObject thisBoss = SkyblockAddons.getInstance().getPersistentValues().getDragonStats();

        if (!thisBoss.has(dragsSinceSupStr))
            thisBoss.addProperty(dragsSinceSupStr, -1);
        getDragsSinceStat(dragsSinceSupStr).setCount(thisBoss.get(dragsSinceSupStr).getAsInt());

        if (!thisBoss.has(dragsSinceAOTDStr))
            thisBoss.addProperty(dragsSinceAOTDStr, -1);
        getDragsSinceStat(dragsSinceAOTDStr).setCount(thisBoss.get(dragsSinceAOTDStr).getAsInt());

        if (!thisBoss.has(dragsSincePetStr))
            thisBoss.addProperty(dragsSincePetStr, -1);
        getDragsSinceStat(dragsSincePetStr).setCount(thisBoss.get(dragsSincePetStr).getAsInt());

        if (!thisBoss.has(eyesPlacedStr))
            thisBoss.addProperty(eyesPlacedStr, 0);
        getDragsSinceStat(eyesPlacedStr).setCount(thisBoss.get(eyesPlacedStr).getAsInt());

        for (int i = 1; i < 5; i++) {
            if (!thisBoss.has("recent" + i))
                thisBoss.addProperty("recent" + i, DragonType.NONE.toString());
            if (DragonType.valueOf(thisBoss.get("recent" + i).getAsString()) != null)
                recent.add(DragonType.valueOf(thisBoss.get("recent" + i).getAsString()));
            else
                recent.add(DragonType.NONE);
        }

    }

    public JsonObject SavePersistentValues() {
        JsonObject returnObj = new JsonObject();
        returnObj.addProperty(dragsSinceSupStr, getDragsSinceStat(dragsSinceSupStr).getCount());
        returnObj.addProperty(dragsSinceAOTDStr, getDragsSinceStat(dragsSinceAOTDStr).getCount());
        returnObj.addProperty(dragsSincePetStr, getDragsSinceStat(dragsSincePetStr).getCount());
        returnObj.addProperty(eyesPlacedStr, getDragsSinceStat(eyesPlacedStr).getCount());
        for (int i = 1; i < 5; i++)
            returnObj.addProperty("recent" + i, recent.get(i - 1).toString());
        return returnObj;
    }

    public enum DragonType {
        NONE("none", ChatFormatting.DARK_GRAY),
        PROTECTOR("dragonProtector", ChatFormatting.DARK_BLUE), OLD("dragonOld", ChatFormatting.GRAY),
        WISE("dragonWise", ChatFormatting.BLUE), UNSTABLE("dragonUnstable", ChatFormatting.BLACK),
        YOUNG("dragonYoung", ChatFormatting.WHITE), STRONG("dragonStrong", ChatFormatting.RED),
        SUPERIOR("dragonSuperior", ChatFormatting.GOLD);

        @Getter
        String name;
        @Getter
        ChatFormatting colour;

        DragonType(String name, ChatFormatting colour) {
            this.name = name;
            this.colour = colour;
        }

        public String getDisplayName() {
            return Utils.getTranslatedString("settings", name);
        }
    }

}

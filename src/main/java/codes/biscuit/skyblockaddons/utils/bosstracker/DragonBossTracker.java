package codes.biscuit.skyblockaddons.utils.bosstracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class DragonBossTracker extends BossTracker {

    public static final String recentDrags = "recentDragons", dragsSince = "dragonsSince";
    public static final String dragsSinceSupStr = "dragonsSinceSuperior";
    public static final String dragsSinceAOTDStr = "dragonsSinceAOTD";
    public static final String dragsSincePetStr = "dragonsSincePet";
    public static final String eyesPlaced = "eyesPlaced";
    /*public int dragsSinceSup;
    public int dragsSinceAOTD;
    public int dragsSincePet;*/
    public int eyePool = 0;

    public ArrayList<Stat> dragsSinceStatMap;

    public DragonBossTracker() {
        super(Feature.DRAGON_STATS_TRACKER, "dragonStats");
        dragsSinceStatMap = new ArrayList<>();
        dragsSinceStatMap.add(new Stat(dragsSinceSupStr, Rarity.LEGENDARY));
        dragsSinceStatMap.add(new Stat(dragsSinceAOTDStr, Rarity.LEGENDARY));
        dragsSinceStatMap.add(new Stat(dragsSincePetStr, Rarity.LEGENDARY));
        dragsSinceStatMap.add(new Stat(eyesPlaced, Rarity.EPIC));
    }

    public Stat getDragsSinceStat(String statName)
    {
        for (Stat stat : dragsSinceStatMap)
            if (statName.equals(stat.getName())) return stat;
        return null;
    }

    @Override
    public ArrayList<Stat> getStats() {
        return dragsSinceStatMap;
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

        if (!thisBoss.has(eyesPlaced))
            thisBoss.addProperty(eyesPlaced, 0);
        getDragsSinceStat(eyesPlaced).setCount(thisBoss.get(eyesPlaced).getAsInt());
    }

    public JsonObject SavePersistentValues() {
        JsonObject returnObj = new JsonObject();
        returnObj.addProperty(dragsSinceSupStr, getDragsSinceStat(dragsSinceSupStr).getCount());
        returnObj.addProperty(dragsSinceAOTDStr, getDragsSinceStat(dragsSinceAOTDStr).getCount());
        returnObj.addProperty(dragsSincePetStr, getDragsSinceStat(dragsSincePetStr).getCount());
        returnObj.addProperty(eyesPlaced, getDragsSinceStat(eyesPlaced).getCount());
        return returnObj;
    }

}

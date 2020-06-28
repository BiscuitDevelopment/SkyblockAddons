package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ItemDiff;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.*;

public class SlayerTracker {

    // TODO Make sure Books work

    @Getter
    private static final SlayerTracker instance = new SlayerTracker();
    public Date stopAcceptingTimestamp, secondPriorTimestamp;
    /**
     * Needed because you can pickup items before it recognises you
     */
    public HashMap<Date, List<ItemDiff>> cache = new HashMap<>();
    @Getter
    private ArrayList<SlayerBoss> bosses = new ArrayList<>();

    public SlayerTracker() {
        bosses.add(new SlayerZombie());
        bosses.add(new SlayerSpider());
        bosses.add(new SlayerWolf());
    }

    public void addSlayerKill(String s) {
        for (SlayerBoss boss : bosses)
            if (s.toLowerCase().contains(boss.getLangName())) {
                boss.setKills(boss.getKills() + 1);
                SkyblockAddons.getInstance().getPersistentValues().saveValues();
                return;
            }
    }

    public void updateDrops(List<ItemDiff> toCheck) {
        boolean changed = false;
        for (ItemDiff diff : toCheck) {
            if (diff.getAmount() < 0) continue;

            boolean found = false;
            System.out.println(diff.getDisplayName().replaceAll("(§([0-9a-fk-or]))", ""));
            for (SlayerBoss boss : bosses) {
                if (found) break;
                for (SlayerBoss.SlayerDrop drop : boss.getDrops())
                    if (diff.getDisplayName().replaceAll("(§([0-9a-fk-or]))", "").equalsIgnoreCase(drop.getActualName())) {
                        changed = true;
                        found = true;
                        drop.setCount(drop.getCount() + diff.getAmount());
                        break;
                    }
            }

        }
        if (changed)
            SkyblockAddons.getInstance().getPersistentValues().saveValues();
    }

    public void useCache() {
        Iterator it = cache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Date, List<ItemDiff>> pair = (Map.Entry<Date, List<ItemDiff>>) it.next();

            if (pair.getKey().after(secondPriorTimestamp)) {
                updateDrops(pair.getValue());
            }
            it.remove();
        }
    }

    public void cleanCache() {
        Iterator it = cache.entrySet().iterator();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, -1);
        Date time = calendar.getTime();
        while (it.hasNext()) {
            Map.Entry<Date, List<ItemDiff>> pair = (Map.Entry<Date, List<ItemDiff>>) it.next();

            if (pair.getKey().before(time))
                it.remove();
        }
    }

    public void LoadPersistentValues() {
        JsonObject slayerDrops = SkyblockAddons.getInstance().getPersistentValues().getSlayerDrops();
        for (SlayerBoss boss : bosses) {
            JsonObject thisBoss;
            if (!slayerDrops.has(boss.getLangName())) {
                thisBoss = new JsonObject();
                slayerDrops.add(boss.getLangName(), thisBoss);
            } else thisBoss = slayerDrops.getAsJsonObject(boss.getLangName());

            if (!thisBoss.has("kills"))
                thisBoss.addProperty("kills", 0);
            boss.setKills(thisBoss.get("kills").getAsInt());

            for (SlayerBoss.SlayerDrop drop : boss.getDrops()) {
                if (!thisBoss.has(drop.getLangName()))
                    thisBoss.addProperty(drop.getLangName(), 0);
                drop.setCount(thisBoss.get(drop.getLangName()).getAsInt());
            }
        }
    }

    public JsonObject SavePersistentValues() {
        JsonObject returnObj = new JsonObject();
        for (SlayerBoss boss : bosses) {
            JsonObject thisBoss = new JsonObject();
            returnObj.add(boss.getLangName(), thisBoss);

            thisBoss.addProperty("kills", boss.getKills());

            for (SlayerBoss.SlayerDrop drop : boss.getDrops()) {
                thisBoss.addProperty(drop.getLangName(), drop.getCount());
            }
        }
        return returnObj;
    }
}

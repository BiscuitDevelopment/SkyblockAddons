package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ItemDiff;
import codes.biscuit.skyblockaddons.utils.item.ItemUtils;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.*;

public class SlayerTracker {

    @Getter
    private static final SlayerTracker instance = new SlayerTracker();
    public Date stopAcceptingTimestamp, secondPriorTimestamp;
    /**
     * Needed because you can pickup items before it recognises you
     */
    public HashMap<Date, List<ItemDiff>> cache = new HashMap<>();
    @Getter
    private ArrayList<SlayerBoss> bosses = new ArrayList<>();
    @Getter
    private SlayerBoss lastKilledBoss;

    /**
     * Add new bosses here, a "feature" setting to the Slayer_Trackers feature,
     * and to the {@link codes.biscuit.skyblockaddons.gui.SettingsGui}#addButton(EnumUtils.FeatureSetting)
     */
    public SlayerTracker() {
        bosses.add(new SlayerZombie());
        bosses.add(new SlayerSpider());
        bosses.add(new SlayerWolf());
    }

    /**
     * Add a kill to the slayer type which is determined by the "Talk to Maddox to claim your <boss> xp message"
     *
     * @param slayerEXPString
     */
    public void addSlayerKill(String slayerEXPString) {
        for (SlayerBoss boss : bosses)
            if (slayerEXPString.toLowerCase().contains(boss.getBossName())) {
                boss.setKills(boss.getKills() + 1);
                SkyblockAddons.getInstance().getPersistentValues().saveValues();
                lastKilledBoss = boss;
                useCache();
                return;
            }
    }

    public void updateDrops(List<ItemDiff> toCheck) {
        boolean changed = false;
        for (ItemDiff diff : toCheck) {
            if (diff.getAmount() < 0) continue;

            for (SlayerBoss.SlayerDrop drop : lastKilledBoss.getDrops())
                //if (diff.getDisplayName().replaceAll("(§([0-9a-fk-or]))", "").equalsIgnoreCase(drop.getActualName())) {
                if (drop.getSkyblockID().split(":")[0].equals(ItemUtils.getSkyBlockItemID(diff.getExtraAttributes()))) {
                    if (drop.getSkyblockID().split(":")[0].equals("RUNE")) {
                        if (ItemUtils.getRuneData(diff.getExtraAttributes()).getType().equals(drop.getSkyblockID().split(":")[1]))
                        {
                            changed = true;
                            drop.setCount(drop.getCount() + diff.getAmount());
                            break;
                        }
                    } else {
                        changed = true;
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
            if (!slayerDrops.has(boss.getBossName())) {
                thisBoss = new JsonObject();
                slayerDrops.add(boss.getBossName(), thisBoss);
            } else thisBoss = slayerDrops.getAsJsonObject(boss.getBossName());

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
            returnObj.add(boss.getBossName(), thisBoss);

            thisBoss.addProperty("kills", boss.getKills());

            for (SlayerBoss.SlayerDrop drop : boss.getDrops()) {
                thisBoss.addProperty(drop.getLangName(), drop.getCount());
            }
        }
        return returnObj;
    }

    public String[] getTabComplete() {
        ArrayList<String> complete = new ArrayList<>();

        for (SlayerBoss boss : bosses)
            complete.add(boss.getBossName());

        return complete.toArray(new String[complete.size()]);
    }

    public String[] getTabCompleteDrops(String s) {
        ArrayList<String> complete = new ArrayList<>();

        for (SlayerBoss boss : bosses) {
            if (!boss.getBossName().equalsIgnoreCase(s)) continue;

            complete.add("kills");
            for (SlayerBoss.SlayerDrop drop : boss.getDrops())
                complete.add(drop.getLangName());

            break;
        }

        return complete.toArray(new String[complete.size()]);
    }

    /**
     * slayer <boss> <stat> <count>
     *
     * @param args
     */
    public void setManual(String[] args) {
        for (SlayerBoss boss : bosses) {
            if (!boss.getBossName().equalsIgnoreCase(args[1])) continue;

            if (args[2].equalsIgnoreCase("kills")) {
                try {
                    int count = Integer.valueOf(args[3]);
                    boss.setKills(count);
                    SkyblockAddons.getInstance().getUtils().sendMessage("Kills for " + args[1] + " set to " + args[3] + ".");
                    SkyblockAddons.getInstance().getPersistentValues().saveValues();
                    return;
                } catch (NumberFormatException ex) {
                    SkyblockAddons.getInstance().getUtils().sendErrorMessage("Invalid number " + args[3] + ".");
                    return;
                }
            }

            for (SlayerBoss.SlayerDrop drop : boss.getDrops()) {
                if (!drop.getLangName().equalsIgnoreCase(args[2])) continue;

                try {
                    int count = Integer.valueOf(args[3]);
                    drop.setCount(count);
                    SkyblockAddons.getInstance().getUtils().sendMessage("Stat " + args[2] + " for " + args[1] + " set to " + args[3] + ".");
                    SkyblockAddons.getInstance().getPersistentValues().saveValues();
                    return;
                } catch (NumberFormatException ex) {
                    SkyblockAddons.getInstance().getUtils().sendErrorMessage("Invalid number " + args[3] + ".");
                    return;
                }
            }

            SkyblockAddons.getInstance().getUtils().sendErrorMessage("Stat " + args[2] + " not found.");
            return;
        }

        SkyblockAddons.getInstance().getUtils().sendErrorMessage("Boss " + args[1] + " not found.");
    }
}

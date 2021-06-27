package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.core.SkillType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillXpManager {

    private static final HashMap<SkillType, SkillXp> skillLevelXp = new HashMap<>();

    public SkillXpManager() {
    }

    public void initialize(JsonInput input) {
        for (Map.Entry<SkillType, List<Integer>> skill : input.entrySet()) {
            skillLevelXp.put(skill.getKey(), new SkillXp(skill.getValue()));
        }
    }

    /**
     * Returns the amount of xp needed to progress from {@code level} to {@code level + 1}
     *
     * @param type
     * @param level
     * @return
     */
    public int getSkillXpForNextLevel(SkillType type, int level) {
        return skillLevelXp.containsKey(type) ? skillLevelXp.get(type).getXpForNextLevel(level) : 0;
    }

    public static class JsonInput extends HashMap<SkillType, List<Integer>> {
    }

    private static class SkillXp {

        /**
         * Look-back values for a given skill level. "How much xp did I need to reach level x?"
         */
        private final List<Integer> cumulativeXp;
        /**
         * Look-ahead values for a given skill level. "How much xp do I need to get from level x to x + 1?"
         */
        private final List<Integer> xpForNext;
        /**
         * The maximum level for this skill
         */
        @Getter
        private final int maxLevel;

        public SkillXp(List<Integer> cumulativeXp) {
            this.cumulativeXp = cumulativeXp;
            maxLevel = cumulativeXp.size() - 1;
            xpForNext = new ArrayList<>(cumulativeXp.size());
            for (int i = 0; i < cumulativeXp.size() - 1; i++) {
                xpForNext.add(cumulativeXp.get(i + 1) - cumulativeXp.get(i));
            }
        }

        public int getXpForNextLevel(int level) {
            return level >= maxLevel || level < 0 ? 0 : xpForNext.get(level);
        }
    }


}

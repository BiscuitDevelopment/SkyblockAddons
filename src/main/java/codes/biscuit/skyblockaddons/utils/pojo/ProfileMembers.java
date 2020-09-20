package codes.biscuit.skyblockaddons.utils.pojo;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class ProfileMembers {

    private HashMap<String, MemberData> members;

    @Getter
    public static class MemberData {

        private Slayers slayer;
        private Stats stats;
    }

    @Getter
    public static class Slayers {

        private SlayerData zombie;
        private SlayerData spider;
        private SlayerData wolf;
    }

    @Getter
    public static class SlayerData {

        private HashMap<Integer, Integer> kills_tier;
    }

    @Getter
    public static class Stats {

        private PetMilestones pet_milestones;
    }

    @Getter
    public static class PetMilestones {

        private int ore_mined;
        private int sea_creatures_killed;
    }
}

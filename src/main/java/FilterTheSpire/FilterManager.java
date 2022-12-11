package FilterTheSpire;

import FilterTheSpire.factory.FilterFactory;
import FilterTheSpire.factory.FilterObject;
import FilterTheSpire.filters.*;
import FilterTheSpire.utils.SeedHelper;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.neow.NeowReward;

import java.util.*;
import java.util.stream.Collectors;

@SpireInitializer
public class FilterManager {
    // Singleton pattern
    private static class FilterManagerHolder { private static final FilterManager INSTANCE = new FilterManager(); }
    private static FilterManager getInstance() { return FilterManagerHolder.INSTANCE; }
    public static void initialize() { getInstance(); }

    private static HashMap<String, AbstractFilter> filters = new HashMap<>();
    private static List<AbstractFilter> sortedList = null;

    // Returns true if all filters pass for the given seed
    public static boolean validateFilters(long seed) {
        return sortedList.stream().allMatch(v -> v.isSeedValid(seed));
    }

    // Sort the filters so we short circuit on the least intensive filters first
    public static void sortFilters(){
        sortedList = new ArrayList<>(filters.values());
        sortedList.sort(Comparator.comparingInt(AbstractFilter::getSortOrder));
    }

    public static boolean hasFilters() {
        return filters.size() > 0;
    }

    public static int numFilters() {
        return filters.size();
    }

    public static HashMap<SeedHelper.RNGType, Integer> preRngCounters = new HashMap<>();

    // --------------------------------------------------------------------------------

    public static void setValidatorFromString(String validatorName, AbstractFilter filter) {
        filters.put(validatorName, filter);
    }

    // --------------------------------------------------------------------------------

    public static void setFilter(FilterObject filterObject){
        String indices = filterObject.possibleEncounterIndices.stream().map(String::valueOf).collect(Collectors.joining(""));
        if (filterObject.possibleValues.size() > 0 || filterObject.secondaryValues.size() > 0){
            AbstractFilter filter = FilterFactory.getAbstractFilterFromFilterObject(filterObject);
            filters.put(filterObject.filterType + indices, filter);
        } else {
            filters.remove(filterObject.filterType + indices);
        }
    }

    public static void setFirstCombatIs(String enemyName) {
        NthCombatFilter filter = new NthCombatFilter(Collections.singletonList(enemyName));
        setValidatorFromString("firstCombatIs", filter);
    }

    // --------------------------------------------------------------------------------

    public static void setFirstBossIs(String bossName) {
        BossFilter filter = new BossFilter(Collections.singletonList(bossName));
        filters.put("firstBoss", filter);
    }

    public static void setFirstBossIsOneOf(ArrayList<String> bossNames) {
        BossFilter filter = new BossFilter(bossNames);
        filters.put("firstBossIsOneOf", filter);
    }

    // --------------------------------------------------------------------------------

    public static void setFirstEliteIs(String eliteName) {
        NthEliteFilter filter = new NthEliteFilter(Collections.singletonList(eliteName));
        filters.put("firstElite", filter);
    }

    public static void setFirstEliteIsOneOf(ArrayList<String> eliteNames) {
        NthEliteFilter filter = new NthEliteFilter(eliteNames);
        filters.put("firstEliteIsOneOf", filter);
    }

    public static void setPandorasCardFilter(HashMap<String, Integer> searchCards) {
        filters.put("pandorasTmp", new PandorasCardFilter(searchCards));
    }

    public static void setAstrolabeCardFilter(HashMap<String, Integer> searchCards) {
        filters.put("astrolabeFilter", new AstrolabeCardFilter(searchCards));
    }

    // --------------------------------------------------------------------------------

    public static void setNthRelicFromValidList(ArrayList<String> relicIds) {
        setNthRelicFromValidList(relicIds, 0);
    }

    public static void setNthRelicFromValidList(ArrayList<String> relicIds, int encounterIndex) {
        if (relicIds.size() > 0) {
            NthRelicFilter filter = new NthRelicFilter(relicIds, encounterIndex);
            filters.put("nthRelicIsOneOf" + encounterIndex, filter);
        } else {
            filters.remove("nthRelicIsOneOf" + encounterIndex);
        }
    }

    public static void setRelicsInEncounters(ArrayList<String> relicIds, List<Integer> encounterIndices) {
        String indices = encounterIndices.stream().map(String::valueOf).collect(Collectors.joining(""));
        if (relicIds.size() > 0) {
            RelicsInEncountersFilter filter = new RelicsInEncountersFilter(relicIds, encounterIndices);
            filters.put("relicInEncounters" + indices, filter);
        } else {
            filters.remove("relicInEncounters" + indices);
        }
    }

    // --------------------------------------------------------------------------------

    public static void setNthCardReward(String searchCard, int combatIndex){
        setNthCardReward(Collections.singletonList(searchCard), combatIndex);
    }

    public static void setNthCardReward(List<String> searchCards, int combatIndex){
        if (searchCards != null && !searchCards.isEmpty()) {
            NthCardRewardFilter filter = new NthCardRewardFilter(searchCards, combatIndex);
            filters.put("nthCardRewardFilter" + combatIndex, filter);
        } else {
            filters.remove("nthCardRewardFilter" + combatIndex);
        }
    }

    // --------------------------------------------------------------------------------

    public static void setBlessingFilter(NeowReward.NeowRewardType blessing, HashMap<String, Integer> cards, NeowReward.NeowRewardDrawback drawback){
        if (blessing == NeowReward.NeowRewardType.RANDOM_COLORLESS_2) {
            preRngCounters.put(SeedHelper.RNGType.CARD, 3);
        }
        BlessingFilter filter = new BlessingFilter(blessing, cards, drawback);
        filters.put("blessingFilter", filter);
    }

    // --------------------------------------------------------------------------------

    public static void setCallingBellFilter(String commonRelic, String uncommonRelic, String rareRelic){
        CallingBellFilter filter = new CallingBellFilter(commonRelic, uncommonRelic, rareRelic);
        filters.put("callingBellFilter", filter);
    }

    public static void print() {
        System.out.println("FilterManager has " + filters.size() + " filters");
    }
}

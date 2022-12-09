package FilterTheSpire.utils;

import FilterTheSpire.factory.FilterObject;
import FilterTheSpire.ui.screens.AlternateConfigMenu;
import basemod.BaseMod;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.*;

import java.io.IOException;
import java.util.*;

public class Config {
    private SpireConfig spireConfig;
    private FilterGroupConfig currentFilters;
    private final String filterKey = "filters";

    public Config() {
        Properties defaults = new Properties();
        defaults.put("bossSwapFilter", "[]");
        defaults.put("shopRelicFilter", "[]");

        try {
            spireConfig = new SpireConfig("FilterTheSpire", "config", defaults);
            spireConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeFilterList();
    }

    private void initializeFilterList(){
        if (spireConfig != null && spireConfig.has(filterKey)) {
            String s = spireConfig.getString(filterKey);
            Gson gson = new Gson();
            currentFilters = gson.fromJson(s, FilterGroupConfig.class);
        } else {
            currentFilters = new FilterGroupConfig(new HashMap<>(), null);
        }
    }

    public FilterObject getFilter(FilterType filterType){
        return currentFilters.activeFilters.getOrDefault(filterType, new FilterObject(filterType));
    }

    public void updateFilter(FilterType filterType, List<String> enabledList){
        FilterObject filter = new FilterObject(filterType, enabledList);
        currentFilters.activeFilters.put(filter.filterType, filter);

        // update settings
        Gson gson = new Gson();
        spireConfig.setString(filterKey, gson.toJson(currentFilters));

        try {
            spireConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBooleanKey(String key, boolean enabled){
        spireConfig.setBool(key, enabled);

        try {
            spireConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getBooleanKeyOrSetDefault(String key, boolean defaultValue){
        if (!spireConfig.has(key)){
            setBooleanKey(key, defaultValue);
        }
        return getBooleanKey(key);
    }

    public boolean getBooleanKey(String key){
        return spireConfig.getBool(key);
    }

    // (Main Menu -> Mods) menu setup
    public static void setupConfigMenu() {
        BaseMod.registerModBadge(new Texture("FilterTheSpire/images/fts_icon.png"),
                "Filter the Spire",
                "ojb",
                "Customize your Slay the Spire experience",
                new AlternateConfigMenu());
    }
}

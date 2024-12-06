package com.eliassen.crucible.taglibrary.helper;

import com.eliassen.crucible.common.helpers.SystemHelper;
import org.json.JSONArray;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TagFunctions
{
    public static String getTagLibrarySettingString(String settingName)
    {
        return SystemHelper.getConfigSetting(TagLibraryConstants.TAG_SETTING_ROOT + settingName);
    }

    public static boolean getTagLibrarySettingBoolean(String settingName)
    {
        return SystemHelper.getConfigSettingBoolean(TagLibraryConstants.TAG_SETTING_ROOT + settingName);
    }

    public static JSONArray getTagLibrarySettingArray(String settingName)
    {
        return SystemHelper.getConfigSettingArray(TagLibraryConstants.TAG_SETTING_ROOT + settingName);
    }

    public static Set<String> getTagsMissingFromControlSet(final Set<String> setToTest, final Set<String> controlSet)
    {
        Set<String> missingTags = setToTest.stream().filter(t -> !controlSet.contains(t)).collect(Collectors.toCollection(TreeSet::new));
        return missingTags;
    }

}

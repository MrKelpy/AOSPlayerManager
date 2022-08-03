package com.mrkelpy.aosplayermanager.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements an "Exclusion Strategy" interface that determines what
 * the Gson Serializer should skip; Specifically Tailor-Made for the serialization of PlayerData.
 * <br>
 * This class can be used through {@link com.google.gson.GsonBuilder#setExclusionStrategies(ExclusionStrategy...)}.
 */
public class PlayerExclusionStrategy implements ExclusionStrategy {

    private static final List<String> FIELD_EXCLUDE = buildExclusionList();

    /**
     * This method returns a list of field names that should be excluded from the serialization.
     * @return Exclusion List
     */
    private static List<String> buildExclusionList() {

        List<String> exclusionList = new ArrayList<>();
        exclusionList.add("serialVersionUID");
        exclusionList.add("CREATOR");
        exclusionList.add("instanceMap");
        exclusionList.add("spigot");

        return exclusionList;
    }

    /**
     * This method determines whether a given field should be skipped or not.
     * @param fieldAttributes The field to be checked
     * @return Whether the class should be skipped or not
     */
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return FIELD_EXCLUDE.contains(fieldAttributes.getName());

    }

    /**
     * This method determines whether a given Object should be skipped or not.
     * @param aClass Class to be checked
     * @return Whether the class should be skipped or not
     */
    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }

}


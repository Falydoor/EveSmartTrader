package com.smarttrader.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Theo on 6/10/16.
 */
public class Referential {
    public static Map<Long, String> GROUP_PARENT_NAME_BY_TYPE_ID = new HashMap<>();

    public static List<Long> SELLABLE_PARENT_GROUP = Arrays.asList(9L, 24L, 150L, 157L, 955L);
}

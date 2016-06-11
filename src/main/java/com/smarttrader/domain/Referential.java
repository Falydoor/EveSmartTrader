package com.smarttrader.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Theo on 6/10/16.
 */
public class Referential {
    public static Map<Long, String> groupParentNameByTypeId = new HashMap<>();

    public static List<Long> sellables = Arrays.asList(9L, 24L, 150L, 157L, 955L);
}

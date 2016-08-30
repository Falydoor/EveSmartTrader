package com.smarttrader.domain.util;

/**
 * Created by Theo on 8/30/16.
 */
public class CrestBuilder {
    private static String CREST_URL = "https://crest-tq.eveonline.com/";

    public static String getMarketOrders(Long regionId) {
        return CREST_URL + "market/" + regionId + "/orders/all/";
    }

    public static String getHistory(Long regionId, Long typeId) {
        return CREST_URL + "market/" + regionId + "/history/?type=" + CREST_URL + "inventory/types/" + typeId + "/";
    }
}

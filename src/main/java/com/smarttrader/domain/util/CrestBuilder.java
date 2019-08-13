package com.smarttrader.domain.util;

/**
 * Created by Theo on 8/30/16.
 */
public class CrestBuilder {
    private static String ESI_URL = "https://esi.evetech.net/latest/";

    public static String getMarketOrders(Long regionId) {
        return ESI_URL + "market/" + regionId + "/orders/all/";
    }

    public static String getHistory(Long regionId, Long typeId) {
        // https://esi.evetech.net/latest/markets/10000002/history/?datasource=tranquility&type_id=34
        return ESI_URL + "markets/" + regionId + "/history/?datasource=tranquility&type_id=" + typeId;
    }
}

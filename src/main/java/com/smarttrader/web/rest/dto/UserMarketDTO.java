package com.smarttrader.web.rest.dto;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Theo on 9/3/16.
 */
public class UserMarketDTO {

    private Set<Integer> sellOrders;

    private Set<Integer> buyOrders;

    public UserMarketDTO() {
        sellOrders = new HashSet<>();
        buyOrders = new HashSet<>();
    }

    public UserMarketDTO(Map<Integer, Set<Integer>> typeIDByBid) {
        sellOrders = typeIDByBid.get(0);
        buyOrders = typeIDByBid.get(1);
    }

    public Set<Integer> getSellOrders() {
        return sellOrders;
    }

    public void setSellOrders(Set<Integer> sellOrders) {
        this.sellOrders = sellOrders;
    }

    public Set<Integer> getBuyOrders() {
        return buyOrders;
    }

    public void setBuyOrders(Set<Integer> buyOrders) {
        this.buyOrders = buyOrders;
    }
}

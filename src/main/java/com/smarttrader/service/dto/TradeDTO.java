package com.smarttrader.service.dto;

import com.smarttrader.domain.InvType;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.Referential;
import com.smarttrader.domain.enums.Station;

import java.util.List;
import java.util.Set;

/**
 * Created by Theo on 6/10/16.
 */
public class TradeDTO {
    private Long sellPrice;

    private Long profit;

    private Long percentProfit;

    private Long totalPrice;

    private Long totalProfit;

    private Long totalQuantity;

    private Long totalVolume;

    private String name;

    private String groupName;

    private String station;

    private Long typeId;

    private Boolean inMarket;

    public TradeDTO(InvType invType, Station station) {
        setCommonFields(invType);
        this.station = station.toString();
        totalVolume = invType.getVolume().longValue();
    }

    public TradeDTO(InvType invType, MarketOrder cheapest, MarketOrder costliest, Set<Long> userMarket) {
        setCommonFields(invType);
        profit = Double.valueOf(cheapest.getPrice() - costliest.getPrice()).longValue();
        sellPrice = costliest.getPrice().longValue();
        percentProfit = 100 * profit / sellPrice;
        inMarket = userMarket.contains(typeId);
    }

    public TradeDTO(InvType invType, List<MarketOrder> sellables, Double cheapestBuyPrice, Double cheapestSellPrice, Set<Long> userMarket, Station station) {
        setCommonFields(invType);
        totalPrice = Double.valueOf(sellables.stream().mapToDouble(value -> value.getPrice() * value.getVolume()).sum()).longValue();
        totalProfit = Double.valueOf(sellables.stream().mapToDouble(value -> (cheapestBuyPrice - value.getPrice()) * value.getVolume()).sum()).longValue();
        totalQuantity = sellables.stream().mapToLong(MarketOrder::getVolume).sum();
        totalVolume = Double.valueOf(totalQuantity * invType.getVolume()).longValue();
        sellPrice = cheapestBuyPrice.longValue();
        percentProfit = 100 * totalProfit / totalPrice;
        profit = Double.valueOf(cheapestBuyPrice - cheapestSellPrice).longValue();
        this.station = station.toString();
        inMarket = userMarket.contains(typeId);
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public Long getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Long sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Long getProfit() {
        return profit;
    }

    public void setProfit(Long profit) {
        this.profit = profit;
    }

    public Long getPercentProfit() {
        return percentProfit;
    }

    public void setPercentProfit(Long percentProfit) {
        this.percentProfit = percentProfit;
    }

    public Long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Long totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Long getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Long totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getInMarket() {
        return inMarket;
    }

    public void setInMarket(Boolean inMarket) {
        this.inMarket = inMarket;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TradeDTO tradeDTO = (TradeDTO) o;

        if (station != null ? !station.equals(tradeDTO.station) : tradeDTO.station != null) return false;
        return typeId != null ? typeId.equals(tradeDTO.typeId) : tradeDTO.typeId == null;

    }

    @Override
    public int hashCode() {
        int result = station != null ? station.hashCode() : 0;
        result = 31 * result + (typeId != null ? typeId.hashCode() : 0);
        return result;
    }

    private void setCommonFields(InvType invType) {
        typeId = invType.getId();
        name = invType.getTypeName();
        groupName = Referential.GROUP_PARENT_NAME_BY_TYPE_ID.get(typeId);
    }
}

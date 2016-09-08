package com.smarttrader.service.dto;

import com.smarttrader.domain.InvType;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.Referential;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.security.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

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

    private Station station;

    private Long typeId;

    private Double thresholdPrice;

    public TradeDTO(SellableInvType sellableInvType) {
        setCommonFields(sellableInvType.getInvType());
        this.station = SecurityUtils.getCurrentUserStation();
        totalVolume = sellableInvType.getInvType().getVolume().longValue();
    }

    public TradeDTO(MarketOrder cheapest, MarketOrder costliest) {
        setCommonFields(cheapest.getInvType());
        profit = Double.valueOf(cheapest.getPrice() - costliest.getPrice()).longValue();
        sellPrice = costliest.getPrice().longValue();
        percentProfit = 100 * profit / sellPrice;
    }

    public TradeDTO(List<MarketOrder> cheapestSell, Double cheapestBuy) {
        sellPrice = cheapestBuy.longValue();
        Double cheapestSellPrice = cheapestSell.get(0).getPrice();
        thresholdPrice = Math.min(cheapestSellPrice * 1.1D, sellPrice);
        List<MarketOrder> sellables = cheapestSell.stream()
            .filter(this::isSellable)
            .collect(Collectors.toList());
        setCommonFields(cheapestSell.get(0).getInvType());
        totalPrice = Double.valueOf(sellables.stream().mapToDouble(this::getTotalPrice).sum()).longValue();
        totalProfit = Double.valueOf(sellables.stream().mapToDouble(this::getTotalProfit).sum()).longValue();
        totalQuantity = sellables.stream().mapToLong(MarketOrder::getVolume).sum();
        totalVolume = Double.valueOf(totalQuantity * cheapestSell.get(0).getInvType().getVolume()).longValue();
        percentProfit = 100 * totalProfit / totalPrice;
        profit = Double.valueOf(sellPrice - cheapestSellPrice).longValue();
        this.station = Station.fromLong(cheapestSell.get(0).getStationID()).orElse(Station.JitaHUB);
    }

    private double getTotalProfit(MarketOrder value) {
        return (sellPrice - value.getPrice()) * value.getVolume();
    }

    private double getTotalPrice(MarketOrder value) {
        return value.getPrice() * value.getVolume();
    }

    private boolean isSellable(MarketOrder marketOrder) {
        return marketOrder.getPrice() < thresholdPrice;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
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

package com.smarttrader.service.dto;

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
}

package com.smarttrader.domain;

import com.google.gson.JsonObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A MarketOrder.
 */
@Entity
@Table(name = "market_order")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "marketorder")
public class MarketOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "buy", nullable = false)
    private Boolean buy;

    @NotNull
    @Column(name = "issued", nullable = false)
    private ZonedDateTime issued;

    @NotNull
    @Column(name = "price", nullable = false)
    private Double price;

    @NotNull
    @Column(name = "volume_entered", nullable = false)
    private Integer volumeEntered;

    @NotNull
    @Column(name = "station_id", nullable = false)
    private Long stationID;

    @NotNull
    @Column(name = "volume", nullable = false)
    private Long volume;

    @NotNull
    @Column(name = "range", nullable = false)
    private String range;

    @NotNull
    @Column(name = "min_volume", nullable = false)
    private Integer minVolume;

    @NotNull
    @Column(name = "duration", nullable = false)
    private Integer duration;

    @ManyToOne
    private InvType invType;

    @ManyToOne
    private SellableInvType sellableInvType;

    public MarketOrder() {
    }

    public MarketOrder(JsonObject json) {
        buy = json.get("buy").getAsBoolean();
        issued = ZonedDateTime.parse(json.get("issued").getAsString() + "+00:00", DateTimeFormatter.ISO_DATE_TIME);
        price = json.get("price").getAsDouble();
        volumeEntered = json.get("volumeEntered").getAsInt();
        stationID = json.get("stationID").getAsLong();
        volume = json.get("volume").getAsLong();
        range = json.get("range").getAsString();
        minVolume = json.get("minVolume").getAsInt();
        duration = json.get("duration").getAsInt();
        id = json.get("id").getAsLong();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    public ZonedDateTime getIssued() {
        return issued;
    }

    public void setIssued(ZonedDateTime issued) {
        this.issued = issued;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getVolumeEntered() {
        return volumeEntered;
    }

    public void setVolumeEntered(Integer volumeEntered) {
        this.volumeEntered = volumeEntered;
    }

    public Long getStationID() {
        return stationID;
    }

    public void setStationID(Long stationID) {
        this.stationID = stationID;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Integer getMinVolume() {
        return minVolume;
    }

    public void setMinVolume(Integer minVolume) {
        this.minVolume = minVolume;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public InvType getInvType() {
        return invType;
    }

    public void setInvType(InvType invType) {
        this.invType = invType;
    }

    public SellableInvType getSellableInvType() {
        return sellableInvType;
    }

    public void setSellableInvType(SellableInvType sellableInvType) {
        this.sellableInvType = sellableInvType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MarketOrder marketOrder = (MarketOrder) o;
        if (marketOrder.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, marketOrder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "MarketOrder{" +
            "id=" + id +
            ", buy='" + buy + "'" +
            ", issued='" + issued + "'" +
            ", price='" + price + "'" +
            ", volumeEntered='" + volumeEntered + "'" +
            ", stationID='" + stationID + "'" +
            ", volume='" + volume + "'" +
            ", range='" + range + "'" +
            ", minVolume='" + minVolume + "'" +
            ", duration='" + duration + "'" +
            '}';
    }
}

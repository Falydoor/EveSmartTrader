package com.smarttrader.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A InvType.
 */
@Entity
@Table(name = "inv_types")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "invtype")
public class InvType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "type_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "group_id")
    private Long groupID;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "description")
    private String description;

    @Column(name = "mass")
    private Double mass;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "capacity")
    private Double capacity;

    @Column(name = "portion_size")
    private Long portionSize;

    @Column(name = "race_id")
    private Long raceID;

    @Column(name = "base_price")
    private Double basePrice;

    @Column(name = "published")
    private Integer published;

    @Column(name = "icon_id")
    private Long iconID;

    @Column(name = "sound_id")
    private Long soundID;

    @Column(name = "graphic_id")
    private Long graphicID;

    @ManyToOne
    @JoinColumn(name = "inv_market_group_id")
    private InvMarketGroup invMarketGroup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupID() {
        return groupID;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMass() {
        return mass;
    }

    public void setMass(Double mass) {
        this.mass = mass;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Long getPortionSize() {
        return portionSize;
    }

    public void setPortionSize(Long portionSize) {
        this.portionSize = portionSize;
    }

    public Long getRaceID() {
        return raceID;
    }

    public void setRaceID(Long raceID) {
        this.raceID = raceID;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getPublished() {
        return published;
    }

    public void setPublished(Integer published) {
        this.published = published;
    }

    public Long getIconID() {
        return iconID;
    }

    public void setIconID(Long iconID) {
        this.iconID = iconID;
    }

    public Long getSoundID() {
        return soundID;
    }

    public void setSoundID(Long soundID) {
        this.soundID = soundID;
    }

    public Long getGraphicID() {
        return graphicID;
    }

    public void setGraphicID(Long graphicID) {
        this.graphicID = graphicID;
    }

    public InvMarketGroup getInvMarketGroup() {
        return invMarketGroup;
    }

    public void setInvMarketGroup(InvMarketGroup invMarketGroup) {
        this.invMarketGroup = invMarketGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InvType invType = (InvType) o;
        if(invType.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, invType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "InvType{" +
            "id=" + id +
            ", groupID='" + groupID + "'" +
            ", typeName='" + typeName + "'" +
            ", description='" + description + "'" +
            ", mass='" + mass + "'" +
            ", volume='" + volume + "'" +
            ", capacity='" + capacity + "'" +
            ", portionSize='" + portionSize + "'" +
            ", raceID='" + raceID + "'" +
            ", basePrice='" + basePrice + "'" +
            ", published='" + published + "'" +
            ", iconID='" + iconID + "'" +
            ", soundID='" + soundID + "'" +
            ", graphicID='" + graphicID + "'" +
            '}';
    }
}

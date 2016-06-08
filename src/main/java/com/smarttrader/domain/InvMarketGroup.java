package com.smarttrader.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A InvMarketGroup.
 */
@Entity
@Table(name = "inv_market_group")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "invmarketgroup")
public class InvMarketGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "parent_group_id")
    private Long parentGroupID;

    @Column(name = "market_group_name")
    private String marketGroupName;

    @Column(name = "description")
    private String description;

    @Column(name = "icon_id")
    private Integer iconID;

    @Column(name = "has_types")
    private Integer hasTypes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentGroupID() {
        return parentGroupID;
    }

    public void setParentGroupID(Long parentGroupID) {
        this.parentGroupID = parentGroupID;
    }

    public String getMarketGroupName() {
        return marketGroupName;
    }

    public void setMarketGroupName(String marketGroupName) {
        this.marketGroupName = marketGroupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIconID() {
        return iconID;
    }

    public void setIconID(Integer iconID) {
        this.iconID = iconID;
    }

    public Integer getHasTypes() {
        return hasTypes;
    }

    public void setHasTypes(Integer hasTypes) {
        this.hasTypes = hasTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InvMarketGroup invMarketGroup = (InvMarketGroup) o;
        if(invMarketGroup.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, invMarketGroup.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "InvMarketGroup{" +
            "id=" + id +
            ", parentGroupID='" + parentGroupID + "'" +
            ", marketGroupName='" + marketGroupName + "'" +
            ", description='" + description + "'" +
            ", iconID='" + iconID + "'" +
            ", hasTypes='" + hasTypes + "'" +
            '}';
    }
}

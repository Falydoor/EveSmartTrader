package com.smarttrader.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A SellableInvType.
 */
@Entity
@Table(name = "sellable_inv_type")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "sellableinvtype")
public class SellableInvType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "sellableInvType")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<MarketOrder> marketOrders = new HashSet<>();

    @ManyToOne
    private InvType invType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<MarketOrder> getMarketOrders() {
        return marketOrders;
    }

    public void setMarketOrders(Set<MarketOrder> marketOrders) {
        this.marketOrders = marketOrders;
    }

    public InvType getInvType() {
        return invType;
    }

    public void setInvType(InvType invType) {
        this.invType = invType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SellableInvType sellableInvType = (SellableInvType) o;
        if(sellableInvType.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, sellableInvType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SellableInvType{" +
            "id=" + id +
            '}';
    }
}

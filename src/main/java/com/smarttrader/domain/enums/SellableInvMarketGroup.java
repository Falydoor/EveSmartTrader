package com.smarttrader.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Theo on 9/2/16.
 */
public enum SellableInvMarketGroup {
    SHIP_EQUIPEMENT(9), IMPLANTS_BOOSTERS(24), SKILLS(150), DRONES(157), SHIP_MODIFICATIONS(955);

    private final long id;

    SellableInvMarketGroup(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public static List<Long> getIds() {
        return Arrays.stream(values()).collect(Collectors.mapping(SellableInvMarketGroup::getId, Collectors.toList()));
    }
}

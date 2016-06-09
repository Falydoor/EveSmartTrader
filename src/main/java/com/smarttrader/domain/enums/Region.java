package com.smarttrader.domain.enums;

/**
 * Created by Theo on 6/8/16.
 */
public enum Region {
    DOMAIN(10000043L), HEIMATAR(10000030L), LONETREK(10000016L), METROPOLIS(10000042L), SINQ_LAISON(10000032L), THE_FORGE(10000002L);

    public static Region fromLong(long regionId) {
        for (Region region : values()) {
            if (regionId == region.getId()) {
                return region;
            }
        }
        return null;
    }

    private final long regionId;

    Region(long regionId) {
        this.regionId = regionId;
    }

    public long getId() {
        return this.regionId;
    }
}

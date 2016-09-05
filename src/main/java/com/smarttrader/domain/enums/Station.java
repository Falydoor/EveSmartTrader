package com.smarttrader.domain.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by Theo on 6/8/16.
 */
public enum Station {
    AmarrHUB(60008494L), DodixieHUB(60011866L), HekHUB(60005686L), JitaHUB(60003760L), RensHUB(60004588L), SobasekiHUB(60003916L);

    public static Optional<Station> fromLong(long stationId) {
        return Arrays.stream(values())
            .filter(station -> station.getId() == stationId)
            .findFirst();
    }

    private final long stationId;

    Station(long stationId) {
        this.stationId = stationId;
    }

    public long getId() {
        return this.stationId;
    }
}

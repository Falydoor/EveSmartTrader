package com.smarttrader.service.builder;

import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.service.dto.TradeDTO;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Theo on 9/9/16.
 */
public class TradeBuilder {

    private final Map<Long, List<MarketOrder>> sellOrdersByStation;
    private final Double cheapestBuy;

    public TradeBuilder(MarketOrder buyMarketOrder, Stream<MarketOrder> sellMarketOrders) {
        sellOrdersByStation = sellMarketOrders.collect(Collectors.groupingBy(MarketOrder::getStationID));
        cheapestBuy = buyMarketOrder.getPrice();
    }

    public List<TradeDTO> buildTrades() {
        return Arrays.stream(Station.values())
            .map(Station::getId)
            .filter(this::isCheapestThanBuyStation)
            .collect(Collectors.mapping(this::getTrade, Collectors.toList()));
    }

    private TradeDTO getTrade(Long stationId) {
        return new TradeDTO(sellOrdersByStation.get(stationId), cheapestBuy);
    }

    private boolean isCheapestThanBuyStation(Long stationId) {
        return !CollectionUtils.isEmpty(sellOrdersByStation.get(stationId)) && sellOrdersByStation.get(stationId).get(0).getPrice() < cheapestBuy;
    }

}

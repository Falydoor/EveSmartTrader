package com.smarttrader.service.builder;

import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.service.dto.TradeDTO;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Theo on 9/9/16.
 */
public class TradeBuilder {

    private final Map<Station, List<MarketOrder>> sellOrdersByStation;
    private final Double cheapestBuy;

    public TradeBuilder(MarketOrder buyMarketOrder, Stream<MarketOrder> sellMarketOrders) {
        sellOrdersByStation = sellMarketOrders.collect(Collectors.groupingBy(marketOrder -> Station.fromLong(marketOrder.getStationID()).get()));
        cheapestBuy = buyMarketOrder.getPrice();
    }

    public TradeDTO getTrade(Station station) {
        return new TradeDTO(sellOrdersByStation.get(station), cheapestBuy);
    }

    public boolean isCheapestThanBuyStation(Station station) {
        return !CollectionUtils.isEmpty(sellOrdersByStation.get(station)) && sellOrdersByStation.get(station).get(0).getPrice() < cheapestBuy;
    }

}

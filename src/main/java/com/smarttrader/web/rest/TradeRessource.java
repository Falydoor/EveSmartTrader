package com.smarttrader.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.security.SecurityUtils;
import com.smarttrader.service.EveApiService;
import com.smarttrader.service.MarketOrderService;
import com.smarttrader.service.dto.TradeDTO;
import com.smarttrader.web.rest.dto.UserMarketDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by Theo on 6/9/16.
 */
@RestController
@RequestMapping("/api")
public class TradeRessource {

    @Inject
    private MarketOrderService marketOrderService;

    @Inject
    private EveApiService eveApiService;

    @RequestMapping(value = "/changeStation",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<UserMarketDTO> changeStation(Station station) {
        SecurityUtils.setBuyStation(station);
        return new ResponseEntity<>(eveApiService.getUserMarketOrders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/hubTrades",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<TradeDTO>> getHubTrades()
        throws URISyntaxException {
        return new ResponseEntity<>(marketOrderService.buildHubTrades(), HttpStatus.OK);
    }

    @RequestMapping(value = "/penuryTrades",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<TradeDTO>> getPenuryTrades()
        throws URISyntaxException {
        return new ResponseEntity<>(marketOrderService.buildPenuryTrades(), HttpStatus.OK);
    }

    @RequestMapping(value = "/stationTrades",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<TradeDTO>> getStationTrades()
        throws URISyntaxException {
        return new ResponseEntity<>(marketOrderService.buildStationTrades(), HttpStatus.OK);
    }

}

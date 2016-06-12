package com.smarttrader.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.service.MarketOrderService;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.net.URISyntaxException;

/**
 * Created by Theo on 6/9/16.
 */
@RestController
@RequestMapping("/api")
public class TradeRessource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Inject
    private MarketOrderService marketOrderService;

    @RequestMapping(value = "/hubTrades",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<JSONArray> getHubTrades(Station station)
        throws URISyntaxException {
        return new ResponseEntity<>(marketOrderService.buildHubTrades(station), HttpStatus.OK);
    }

    @RequestMapping(value = "/penuryTrades",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<JSONArray> getPenuryTrades(Station station)
        throws URISyntaxException {
        return new ResponseEntity<>(marketOrderService.buildPenuryTrades(station), HttpStatus.OK);
    }

    @RequestMapping(value = "/stationTrades",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<JSONArray> getStationTrades(Station station)
        throws URISyntaxException {
        return new ResponseEntity<>(marketOrderService.buildStationTrades(station), HttpStatus.OK);
    }

}

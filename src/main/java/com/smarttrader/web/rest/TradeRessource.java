package com.smarttrader.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.security.SecurityUtils;
import com.smarttrader.service.MarketOrderService;
import org.json.JSONArray;
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

    @Inject
    private MarketOrderService marketOrderService;

    @RequestMapping(value = "/changeStation",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public void changeStation(Station station) {
        SecurityUtils.setCurrentUserStation(station);
    }

    @RequestMapping(value = "/hubTrades",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<JSONArray> getHubTrades()
        throws URISyntaxException {
        return new ResponseEntity<>(new JSONArray(marketOrderService.buildHubTrades()), HttpStatus.OK);
    }

    @RequestMapping(value = "/penuryTrades",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<JSONArray> getPenuryTrades()
        throws URISyntaxException {
        return new ResponseEntity<>(new JSONArray(marketOrderService.buildPenuryTrades()), HttpStatus.OK);
    }

    @RequestMapping(value = "/stationTrades",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<JSONArray> getStationTrades()
        throws URISyntaxException {
        return new ResponseEntity<>(new JSONArray(marketOrderService.buildStationTrades()), HttpStatus.OK);
    }

}

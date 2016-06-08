package com.smarttrader.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.search.MarketOrderSearchRepository;
import com.smarttrader.web.rest.util.HeaderUtil;
import com.smarttrader.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing MarketOrder.
 */
@RestController
@RequestMapping("/api")
public class MarketOrderResource {

    private final Logger log = LoggerFactory.getLogger(MarketOrderResource.class);
        
    @Inject
    private MarketOrderRepository marketOrderRepository;
    
    @Inject
    private MarketOrderSearchRepository marketOrderSearchRepository;
    
    /**
     * POST  /market-orders : Create a new marketOrder.
     *
     * @param marketOrder the marketOrder to create
     * @return the ResponseEntity with status 201 (Created) and with body the new marketOrder, or with status 400 (Bad Request) if the marketOrder has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/market-orders",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<MarketOrder> createMarketOrder(@Valid @RequestBody MarketOrder marketOrder) throws URISyntaxException {
        log.debug("REST request to save MarketOrder : {}", marketOrder);
        if (marketOrder.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("marketOrder", "idexists", "A new marketOrder cannot already have an ID")).body(null);
        }
        MarketOrder result = marketOrderRepository.save(marketOrder);
        marketOrderSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/market-orders/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("marketOrder", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /market-orders : Updates an existing marketOrder.
     *
     * @param marketOrder the marketOrder to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated marketOrder,
     * or with status 400 (Bad Request) if the marketOrder is not valid,
     * or with status 500 (Internal Server Error) if the marketOrder couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/market-orders",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<MarketOrder> updateMarketOrder(@Valid @RequestBody MarketOrder marketOrder) throws URISyntaxException {
        log.debug("REST request to update MarketOrder : {}", marketOrder);
        if (marketOrder.getId() == null) {
            return createMarketOrder(marketOrder);
        }
        MarketOrder result = marketOrderRepository.save(marketOrder);
        marketOrderSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("marketOrder", marketOrder.getId().toString()))
            .body(result);
    }

    /**
     * GET  /market-orders : get all the marketOrders.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of marketOrders in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/market-orders",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<MarketOrder>> getAllMarketOrders(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of MarketOrders");
        Page<MarketOrder> page = marketOrderRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/market-orders");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /market-orders/:id : get the "id" marketOrder.
     *
     * @param id the id of the marketOrder to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the marketOrder, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/market-orders/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<MarketOrder> getMarketOrder(@PathVariable Long id) {
        log.debug("REST request to get MarketOrder : {}", id);
        MarketOrder marketOrder = marketOrderRepository.findOne(id);
        return Optional.ofNullable(marketOrder)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /market-orders/:id : delete the "id" marketOrder.
     *
     * @param id the id of the marketOrder to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/market-orders/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteMarketOrder(@PathVariable Long id) {
        log.debug("REST request to delete MarketOrder : {}", id);
        marketOrderRepository.delete(id);
        marketOrderSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("marketOrder", id.toString())).build();
    }

    /**
     * SEARCH  /_search/market-orders?query=:query : search for the marketOrder corresponding
     * to the query.
     *
     * @param query the query of the marketOrder search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/market-orders",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<MarketOrder>> searchMarketOrders(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of MarketOrders for query {}", query);
        Page<MarketOrder> page = marketOrderSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/market-orders");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}

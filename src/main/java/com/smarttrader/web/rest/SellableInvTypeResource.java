package com.smarttrader.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.repository.SellableInvTypeRepository;
import com.smarttrader.repository.search.SellableInvTypeSearchRepository;
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
 * REST controller for managing SellableInvType.
 */
@RestController
@RequestMapping("/api")
public class SellableInvTypeResource {

    private final Logger log = LoggerFactory.getLogger(SellableInvTypeResource.class);
        
    @Inject
    private SellableInvTypeRepository sellableInvTypeRepository;
    
    @Inject
    private SellableInvTypeSearchRepository sellableInvTypeSearchRepository;
    
    /**
     * POST  /sellable-inv-types : Create a new sellableInvType.
     *
     * @param sellableInvType the sellableInvType to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sellableInvType, or with status 400 (Bad Request) if the sellableInvType has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/sellable-inv-types",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<SellableInvType> createSellableInvType(@Valid @RequestBody SellableInvType sellableInvType) throws URISyntaxException {
        log.debug("REST request to save SellableInvType : {}", sellableInvType);
        if (sellableInvType.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("sellableInvType", "idexists", "A new sellableInvType cannot already have an ID")).body(null);
        }
        SellableInvType result = sellableInvTypeRepository.save(sellableInvType);
        sellableInvTypeSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/sellable-inv-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("sellableInvType", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /sellable-inv-types : Updates an existing sellableInvType.
     *
     * @param sellableInvType the sellableInvType to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sellableInvType,
     * or with status 400 (Bad Request) if the sellableInvType is not valid,
     * or with status 500 (Internal Server Error) if the sellableInvType couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/sellable-inv-types",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<SellableInvType> updateSellableInvType(@Valid @RequestBody SellableInvType sellableInvType) throws URISyntaxException {
        log.debug("REST request to update SellableInvType : {}", sellableInvType);
        if (sellableInvType.getId() == null) {
            return createSellableInvType(sellableInvType);
        }
        SellableInvType result = sellableInvTypeRepository.save(sellableInvType);
        sellableInvTypeSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("sellableInvType", sellableInvType.getId().toString()))
            .body(result);
    }

    /**
     * GET  /sellable-inv-types : get all the sellableInvTypes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of sellableInvTypes in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/sellable-inv-types",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<SellableInvType>> getAllSellableInvTypes(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of SellableInvTypes");
        Page<SellableInvType> page = sellableInvTypeRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/sellable-inv-types");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /sellable-inv-types/:id : get the "id" sellableInvType.
     *
     * @param id the id of the sellableInvType to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sellableInvType, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/sellable-inv-types/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<SellableInvType> getSellableInvType(@PathVariable Long id) {
        log.debug("REST request to get SellableInvType : {}", id);
        SellableInvType sellableInvType = sellableInvTypeRepository.findOne(id);
        return Optional.ofNullable(sellableInvType)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /sellable-inv-types/:id : delete the "id" sellableInvType.
     *
     * @param id the id of the sellableInvType to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/sellable-inv-types/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteSellableInvType(@PathVariable Long id) {
        log.debug("REST request to delete SellableInvType : {}", id);
        sellableInvTypeRepository.delete(id);
        sellableInvTypeSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("sellableInvType", id.toString())).build();
    }

    /**
     * SEARCH  /_search/sellable-inv-types?query=:query : search for the sellableInvType corresponding
     * to the query.
     *
     * @param query the query of the sellableInvType search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/sellable-inv-types",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<SellableInvType>> searchSellableInvTypes(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of SellableInvTypes for query {}", query);
        Page<SellableInvType> page = sellableInvTypeSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/sellable-inv-types");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}

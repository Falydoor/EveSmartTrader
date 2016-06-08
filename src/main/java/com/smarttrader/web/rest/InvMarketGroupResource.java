package com.smarttrader.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.smarttrader.domain.InvMarketGroup;
import com.smarttrader.repository.InvMarketGroupRepository;
import com.smarttrader.repository.search.InvMarketGroupSearchRepository;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing InvMarketGroup.
 */
@RestController
@RequestMapping("/api")
public class InvMarketGroupResource {

    private final Logger log = LoggerFactory.getLogger(InvMarketGroupResource.class);
        
    @Inject
    private InvMarketGroupRepository invMarketGroupRepository;
    
    @Inject
    private InvMarketGroupSearchRepository invMarketGroupSearchRepository;
    
    /**
     * POST  /inv-market-groups : Create a new invMarketGroup.
     *
     * @param invMarketGroup the invMarketGroup to create
     * @return the ResponseEntity with status 201 (Created) and with body the new invMarketGroup, or with status 400 (Bad Request) if the invMarketGroup has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/inv-market-groups",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<InvMarketGroup> createInvMarketGroup(@RequestBody InvMarketGroup invMarketGroup) throws URISyntaxException {
        log.debug("REST request to save InvMarketGroup : {}", invMarketGroup);
        if (invMarketGroup.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("invMarketGroup", "idexists", "A new invMarketGroup cannot already have an ID")).body(null);
        }
        InvMarketGroup result = invMarketGroupRepository.save(invMarketGroup);
        invMarketGroupSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/inv-market-groups/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("invMarketGroup", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /inv-market-groups : Updates an existing invMarketGroup.
     *
     * @param invMarketGroup the invMarketGroup to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated invMarketGroup,
     * or with status 400 (Bad Request) if the invMarketGroup is not valid,
     * or with status 500 (Internal Server Error) if the invMarketGroup couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/inv-market-groups",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<InvMarketGroup> updateInvMarketGroup(@RequestBody InvMarketGroup invMarketGroup) throws URISyntaxException {
        log.debug("REST request to update InvMarketGroup : {}", invMarketGroup);
        if (invMarketGroup.getId() == null) {
            return createInvMarketGroup(invMarketGroup);
        }
        InvMarketGroup result = invMarketGroupRepository.save(invMarketGroup);
        invMarketGroupSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("invMarketGroup", invMarketGroup.getId().toString()))
            .body(result);
    }

    /**
     * GET  /inv-market-groups : get all the invMarketGroups.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of invMarketGroups in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/inv-market-groups",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<InvMarketGroup>> getAllInvMarketGroups(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of InvMarketGroups");
        Page<InvMarketGroup> page = invMarketGroupRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/inv-market-groups");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /inv-market-groups/:id : get the "id" invMarketGroup.
     *
     * @param id the id of the invMarketGroup to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the invMarketGroup, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/inv-market-groups/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<InvMarketGroup> getInvMarketGroup(@PathVariable Long id) {
        log.debug("REST request to get InvMarketGroup : {}", id);
        InvMarketGroup invMarketGroup = invMarketGroupRepository.findOne(id);
        return Optional.ofNullable(invMarketGroup)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /inv-market-groups/:id : delete the "id" invMarketGroup.
     *
     * @param id the id of the invMarketGroup to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/inv-market-groups/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteInvMarketGroup(@PathVariable Long id) {
        log.debug("REST request to delete InvMarketGroup : {}", id);
        invMarketGroupRepository.delete(id);
        invMarketGroupSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("invMarketGroup", id.toString())).build();
    }

    /**
     * SEARCH  /_search/inv-market-groups?query=:query : search for the invMarketGroup corresponding
     * to the query.
     *
     * @param query the query of the invMarketGroup search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/inv-market-groups",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<InvMarketGroup>> searchInvMarketGroups(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of InvMarketGroups for query {}", query);
        Page<InvMarketGroup> page = invMarketGroupSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/inv-market-groups");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}

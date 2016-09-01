package com.smarttrader.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.smarttrader.domain.InvType;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.search.InvTypeSearchRepository;
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

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * REST controller for managing InvType.
 */
@RestController
@RequestMapping("/api")
public class InvTypeResource {

    private final Logger log = LoggerFactory.getLogger(InvTypeResource.class);

    @Inject
    private InvTypeRepository invTypeRepository;

    @Inject
    private InvTypeSearchRepository invTypeSearchRepository;

    /**
     * POST  /inv-types : Create a new invType.
     *
     * @param invType the invType to create
     * @return the ResponseEntity with status 201 (Created) and with body the new invType, or with status 400 (Bad Request) if the invType has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/inv-types",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<InvType> createInvType(@RequestBody InvType invType) throws URISyntaxException {
        log.debug("REST request to save InvType : {}", invType);
        if (invType.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("invType", "idexists", "A new invType cannot already have an ID")).body(null);
        }
        InvType result = invTypeRepository.save(invType);
        invTypeSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/inv-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("invType", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /inv-types : Updates an existing invType.
     *
     * @param invType the invType to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated invType,
     * or with status 400 (Bad Request) if the invType is not valid,
     * or with status 500 (Internal Server Error) if the invType couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/inv-types",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<InvType> updateInvType(@RequestBody InvType invType) throws URISyntaxException {
        log.debug("REST request to update InvType : {}", invType);
        if (invType.getId() == null) {
            return createInvType(invType);
        }
        InvType result = invTypeRepository.save(invType);
        invTypeSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("invType", invType.getId().toString()))
            .body(result);
    }

    /**
     * GET  /inv-types : get all the invTypes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of invTypes in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/inv-types",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<InvType>> getAllInvTypes(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of InvTypes");
        Page<InvType> page = invTypeRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/inv-types");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /inv-types/:id : get the "id" invType.
     *
     * @param id the id of the invType to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the invType, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/inv-types/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<InvType> getInvType(@PathVariable Long id) {
        log.debug("REST request to get InvType : {}", id);
        InvType invType = invTypeRepository.findOne(id);
        return Optional.ofNullable(invType)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /inv-types/:id : delete the "id" invType.
     *
     * @param id the id of the invType to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/inv-types/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteInvType(@PathVariable Long id) {
        log.debug("REST request to delete InvType : {}", id);
        invTypeRepository.delete(id);
        invTypeSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("invType", id.toString())).build();
    }

    /**
     * SEARCH  /_search/inv-types?query=:query : search for the invType corresponding
     * to the query.
     *
     * @param query the query of the invType search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/inv-types",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<InvType>> searchInvTypes(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of InvTypes for query {}", query);
        Page<InvType> page = invTypeSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/inv-types");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}

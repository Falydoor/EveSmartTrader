package com.smarttrader.web.rest;

import com.smarttrader.EveSmartTraderApp;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.repository.SellableInvTypeRepository;
import com.smarttrader.repository.search.SellableInvTypeSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the SellableInvTypeResource REST controller.
 *
 * @see SellableInvTypeResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EveSmartTraderApp.class)
@WebAppConfiguration
@IntegrationTest
public class SellableInvTypeResourceIntTest {


    private static final Boolean DEFAULT_SELLABLE = false;
    private static final Boolean UPDATED_SELLABLE = true;

    @Inject
    private SellableInvTypeRepository sellableInvTypeRepository;

    @Inject
    private SellableInvTypeSearchRepository sellableInvTypeSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restSellableInvTypeMockMvc;

    private SellableInvType sellableInvType;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        SellableInvTypeResource sellableInvTypeResource = new SellableInvTypeResource();
        ReflectionTestUtils.setField(sellableInvTypeResource, "sellableInvTypeSearchRepository", sellableInvTypeSearchRepository);
        ReflectionTestUtils.setField(sellableInvTypeResource, "sellableInvTypeRepository", sellableInvTypeRepository);
        this.restSellableInvTypeMockMvc = MockMvcBuilders.standaloneSetup(sellableInvTypeResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        sellableInvTypeSearchRepository.deleteAll();
        sellableInvType = new SellableInvType();
        sellableInvType.setSellable(DEFAULT_SELLABLE);
    }

    @Test
    @Transactional
    public void createSellableInvType() throws Exception {
        int databaseSizeBeforeCreate = sellableInvTypeRepository.findAll().size();

        // Create the SellableInvType

        restSellableInvTypeMockMvc.perform(post("/api/sellable-inv-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sellableInvType)))
                .andExpect(status().isCreated());

        // Validate the SellableInvType in the database
        List<SellableInvType> sellableInvTypes = sellableInvTypeRepository.findAll();
        assertThat(sellableInvTypes).hasSize(databaseSizeBeforeCreate + 1);
        SellableInvType testSellableInvType = sellableInvTypes.get(sellableInvTypes.size() - 1);
        assertThat(testSellableInvType.isSellable()).isEqualTo(DEFAULT_SELLABLE);

        // Validate the SellableInvType in ElasticSearch
        SellableInvType sellableInvTypeEs = sellableInvTypeSearchRepository.findOne(testSellableInvType.getId());
        assertThat(sellableInvTypeEs).isEqualToComparingFieldByField(testSellableInvType);
    }

    @Test
    @Transactional
    public void checkSellableIsRequired() throws Exception {
        int databaseSizeBeforeTest = sellableInvTypeRepository.findAll().size();
        // set the field null
        sellableInvType.setSellable(null);

        // Create the SellableInvType, which fails.

        restSellableInvTypeMockMvc.perform(post("/api/sellable-inv-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sellableInvType)))
                .andExpect(status().isBadRequest());

        List<SellableInvType> sellableInvTypes = sellableInvTypeRepository.findAll();
        assertThat(sellableInvTypes).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSellableInvTypes() throws Exception {
        // Initialize the database
        sellableInvTypeRepository.saveAndFlush(sellableInvType);

        // Get all the sellableInvTypes
        restSellableInvTypeMockMvc.perform(get("/api/sellable-inv-types?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(sellableInvType.getId().intValue())))
                .andExpect(jsonPath("$.[*].sellable").value(hasItem(DEFAULT_SELLABLE.booleanValue())));
    }

    @Test
    @Transactional
    public void getSellableInvType() throws Exception {
        // Initialize the database
        sellableInvTypeRepository.saveAndFlush(sellableInvType);

        // Get the sellableInvType
        restSellableInvTypeMockMvc.perform(get("/api/sellable-inv-types/{id}", sellableInvType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(sellableInvType.getId().intValue()))
            .andExpect(jsonPath("$.sellable").value(DEFAULT_SELLABLE.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingSellableInvType() throws Exception {
        // Get the sellableInvType
        restSellableInvTypeMockMvc.perform(get("/api/sellable-inv-types/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSellableInvType() throws Exception {
        // Initialize the database
        sellableInvTypeRepository.saveAndFlush(sellableInvType);
        sellableInvTypeSearchRepository.save(sellableInvType);
        int databaseSizeBeforeUpdate = sellableInvTypeRepository.findAll().size();

        // Update the sellableInvType
        SellableInvType updatedSellableInvType = new SellableInvType();
        updatedSellableInvType.setId(sellableInvType.getId());
        updatedSellableInvType.setSellable(UPDATED_SELLABLE);

        restSellableInvTypeMockMvc.perform(put("/api/sellable-inv-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedSellableInvType)))
                .andExpect(status().isOk());

        // Validate the SellableInvType in the database
        List<SellableInvType> sellableInvTypes = sellableInvTypeRepository.findAll();
        assertThat(sellableInvTypes).hasSize(databaseSizeBeforeUpdate);
        SellableInvType testSellableInvType = sellableInvTypes.get(sellableInvTypes.size() - 1);
        assertThat(testSellableInvType.isSellable()).isEqualTo(UPDATED_SELLABLE);

        // Validate the SellableInvType in ElasticSearch
        SellableInvType sellableInvTypeEs = sellableInvTypeSearchRepository.findOne(testSellableInvType.getId());
        assertThat(sellableInvTypeEs).isEqualToComparingFieldByField(testSellableInvType);
    }

    @Test
    @Transactional
    public void deleteSellableInvType() throws Exception {
        // Initialize the database
        sellableInvTypeRepository.saveAndFlush(sellableInvType);
        sellableInvTypeSearchRepository.save(sellableInvType);
        int databaseSizeBeforeDelete = sellableInvTypeRepository.findAll().size();

        // Get the sellableInvType
        restSellableInvTypeMockMvc.perform(delete("/api/sellable-inv-types/{id}", sellableInvType.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean sellableInvTypeExistsInEs = sellableInvTypeSearchRepository.exists(sellableInvType.getId());
        assertThat(sellableInvTypeExistsInEs).isFalse();

        // Validate the database is empty
        List<SellableInvType> sellableInvTypes = sellableInvTypeRepository.findAll();
        assertThat(sellableInvTypes).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchSellableInvType() throws Exception {
        // Initialize the database
        sellableInvTypeRepository.saveAndFlush(sellableInvType);
        sellableInvTypeSearchRepository.save(sellableInvType);

        // Search the sellableInvType
        restSellableInvTypeMockMvc.perform(get("/api/_search/sellable-inv-types?query=id:" + sellableInvType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sellableInvType.getId().intValue())))
            .andExpect(jsonPath("$.[*].sellable").value(hasItem(DEFAULT_SELLABLE.booleanValue())));
    }
}

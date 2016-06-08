package com.smarttrader.web.rest;

import com.smarttrader.EveSmartTraderApp;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.search.MarketOrderSearchRepository;

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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the MarketOrderResource REST controller.
 *
 * @see MarketOrderResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EveSmartTraderApp.class)
@WebAppConfiguration
@IntegrationTest
public class MarketOrderResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));


    private static final Boolean DEFAULT_BUY = false;
    private static final Boolean UPDATED_BUY = true;

    private static final ZonedDateTime DEFAULT_ISSUED = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_ISSUED = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_ISSUED_STR = dateTimeFormatter.format(DEFAULT_ISSUED);

    private static final Double DEFAULT_PRICE = 1D;
    private static final Double UPDATED_PRICE = 2D;

    private static final Double DEFAULT_VOLUME_ENTERED = 1D;
    private static final Double UPDATED_VOLUME_ENTERED = 2D;

    private static final Long DEFAULT_STATION_ID = 1L;
    private static final Long UPDATED_STATION_ID = 2L;

    private static final Long DEFAULT_VOLUME = 1L;
    private static final Long UPDATED_VOLUME = 2L;
    private static final String DEFAULT_RANGE = "AAAAA";
    private static final String UPDATED_RANGE = "BBBBB";

    private static final Double DEFAULT_MIN_VOLUME = 1D;
    private static final Double UPDATED_MIN_VOLUME = 2D;

    private static final Integer DEFAULT_DURATION = 1;
    private static final Integer UPDATED_DURATION = 2;

    @Inject
    private MarketOrderRepository marketOrderRepository;

    @Inject
    private MarketOrderSearchRepository marketOrderSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restMarketOrderMockMvc;

    private MarketOrder marketOrder;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MarketOrderResource marketOrderResource = new MarketOrderResource();
        ReflectionTestUtils.setField(marketOrderResource, "marketOrderSearchRepository", marketOrderSearchRepository);
        ReflectionTestUtils.setField(marketOrderResource, "marketOrderRepository", marketOrderRepository);
        this.restMarketOrderMockMvc = MockMvcBuilders.standaloneSetup(marketOrderResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        marketOrderSearchRepository.deleteAll();
        marketOrder = new MarketOrder();
        marketOrder.setBuy(DEFAULT_BUY);
        marketOrder.setIssued(DEFAULT_ISSUED);
        marketOrder.setPrice(DEFAULT_PRICE);
        marketOrder.setVolumeEntered(DEFAULT_VOLUME_ENTERED);
        marketOrder.setStationID(DEFAULT_STATION_ID);
        marketOrder.setVolume(DEFAULT_VOLUME);
        marketOrder.setRange(DEFAULT_RANGE);
        marketOrder.setMinVolume(DEFAULT_MIN_VOLUME);
        marketOrder.setDuration(DEFAULT_DURATION);
    }

    @Test
    @Transactional
    public void createMarketOrder() throws Exception {
        int databaseSizeBeforeCreate = marketOrderRepository.findAll().size();

        // Create the MarketOrder

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isCreated());

        // Validate the MarketOrder in the database
        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeCreate + 1);
        MarketOrder testMarketOrder = marketOrders.get(marketOrders.size() - 1);
        assertThat(testMarketOrder.isBuy()).isEqualTo(DEFAULT_BUY);
        assertThat(testMarketOrder.getIssued()).isEqualTo(DEFAULT_ISSUED);
        assertThat(testMarketOrder.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testMarketOrder.getVolumeEntered()).isEqualTo(DEFAULT_VOLUME_ENTERED);
        assertThat(testMarketOrder.getStationID()).isEqualTo(DEFAULT_STATION_ID);
        assertThat(testMarketOrder.getVolume()).isEqualTo(DEFAULT_VOLUME);
        assertThat(testMarketOrder.getRange()).isEqualTo(DEFAULT_RANGE);
        assertThat(testMarketOrder.getMinVolume()).isEqualTo(DEFAULT_MIN_VOLUME);
        assertThat(testMarketOrder.getDuration()).isEqualTo(DEFAULT_DURATION);

        // Validate the MarketOrder in ElasticSearch
        MarketOrder marketOrderEs = marketOrderSearchRepository.findOne(testMarketOrder.getId());
        assertThat(marketOrderEs).isEqualToComparingFieldByField(testMarketOrder);
    }

    @Test
    @Transactional
    public void checkBuyIsRequired() throws Exception {
        int databaseSizeBeforeTest = marketOrderRepository.findAll().size();
        // set the field null
        marketOrder.setBuy(null);

        // Create the MarketOrder, which fails.

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isBadRequest());

        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkIssuedIsRequired() throws Exception {
        int databaseSizeBeforeTest = marketOrderRepository.findAll().size();
        // set the field null
        marketOrder.setIssued(null);

        // Create the MarketOrder, which fails.

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isBadRequest());

        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPriceIsRequired() throws Exception {
        int databaseSizeBeforeTest = marketOrderRepository.findAll().size();
        // set the field null
        marketOrder.setPrice(null);

        // Create the MarketOrder, which fails.

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isBadRequest());

        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVolumeEnteredIsRequired() throws Exception {
        int databaseSizeBeforeTest = marketOrderRepository.findAll().size();
        // set the field null
        marketOrder.setVolumeEntered(null);

        // Create the MarketOrder, which fails.

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isBadRequest());

        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStationIDIsRequired() throws Exception {
        int databaseSizeBeforeTest = marketOrderRepository.findAll().size();
        // set the field null
        marketOrder.setStationID(null);

        // Create the MarketOrder, which fails.

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isBadRequest());

        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVolumeIsRequired() throws Exception {
        int databaseSizeBeforeTest = marketOrderRepository.findAll().size();
        // set the field null
        marketOrder.setVolume(null);

        // Create the MarketOrder, which fails.

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isBadRequest());

        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRangeIsRequired() throws Exception {
        int databaseSizeBeforeTest = marketOrderRepository.findAll().size();
        // set the field null
        marketOrder.setRange(null);

        // Create the MarketOrder, which fails.

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isBadRequest());

        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkMinVolumeIsRequired() throws Exception {
        int databaseSizeBeforeTest = marketOrderRepository.findAll().size();
        // set the field null
        marketOrder.setMinVolume(null);

        // Create the MarketOrder, which fails.

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isBadRequest());

        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDurationIsRequired() throws Exception {
        int databaseSizeBeforeTest = marketOrderRepository.findAll().size();
        // set the field null
        marketOrder.setDuration(null);

        // Create the MarketOrder, which fails.

        restMarketOrderMockMvc.perform(post("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marketOrder)))
                .andExpect(status().isBadRequest());

        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllMarketOrders() throws Exception {
        // Initialize the database
        marketOrderRepository.saveAndFlush(marketOrder);

        // Get all the marketOrders
        restMarketOrderMockMvc.perform(get("/api/market-orders?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(marketOrder.getId().intValue())))
                .andExpect(jsonPath("$.[*].buy").value(hasItem(DEFAULT_BUY.booleanValue())))
                .andExpect(jsonPath("$.[*].issued").value(hasItem(DEFAULT_ISSUED_STR)))
                .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
                .andExpect(jsonPath("$.[*].volumeEntered").value(hasItem(DEFAULT_VOLUME_ENTERED.doubleValue())))
                .andExpect(jsonPath("$.[*].stationID").value(hasItem(DEFAULT_STATION_ID.intValue())))
                .andExpect(jsonPath("$.[*].volume").value(hasItem(DEFAULT_VOLUME.intValue())))
                .andExpect(jsonPath("$.[*].range").value(hasItem(DEFAULT_RANGE.toString())))
                .andExpect(jsonPath("$.[*].minVolume").value(hasItem(DEFAULT_MIN_VOLUME.doubleValue())))
                .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)));
    }

    @Test
    @Transactional
    public void getMarketOrder() throws Exception {
        // Initialize the database
        marketOrderRepository.saveAndFlush(marketOrder);

        // Get the marketOrder
        restMarketOrderMockMvc.perform(get("/api/market-orders/{id}", marketOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(marketOrder.getId().intValue()))
            .andExpect(jsonPath("$.buy").value(DEFAULT_BUY.booleanValue()))
            .andExpect(jsonPath("$.issued").value(DEFAULT_ISSUED_STR))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.doubleValue()))
            .andExpect(jsonPath("$.volumeEntered").value(DEFAULT_VOLUME_ENTERED.doubleValue()))
            .andExpect(jsonPath("$.stationID").value(DEFAULT_STATION_ID.intValue()))
            .andExpect(jsonPath("$.volume").value(DEFAULT_VOLUME.intValue()))
            .andExpect(jsonPath("$.range").value(DEFAULT_RANGE.toString()))
            .andExpect(jsonPath("$.minVolume").value(DEFAULT_MIN_VOLUME.doubleValue()))
            .andExpect(jsonPath("$.duration").value(DEFAULT_DURATION));
    }

    @Test
    @Transactional
    public void getNonExistingMarketOrder() throws Exception {
        // Get the marketOrder
        restMarketOrderMockMvc.perform(get("/api/market-orders/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMarketOrder() throws Exception {
        // Initialize the database
        marketOrderRepository.saveAndFlush(marketOrder);
        marketOrderSearchRepository.save(marketOrder);
        int databaseSizeBeforeUpdate = marketOrderRepository.findAll().size();

        // Update the marketOrder
        MarketOrder updatedMarketOrder = new MarketOrder();
        updatedMarketOrder.setId(marketOrder.getId());
        updatedMarketOrder.setBuy(UPDATED_BUY);
        updatedMarketOrder.setIssued(UPDATED_ISSUED);
        updatedMarketOrder.setPrice(UPDATED_PRICE);
        updatedMarketOrder.setVolumeEntered(UPDATED_VOLUME_ENTERED);
        updatedMarketOrder.setStationID(UPDATED_STATION_ID);
        updatedMarketOrder.setVolume(UPDATED_VOLUME);
        updatedMarketOrder.setRange(UPDATED_RANGE);
        updatedMarketOrder.setMinVolume(UPDATED_MIN_VOLUME);
        updatedMarketOrder.setDuration(UPDATED_DURATION);

        restMarketOrderMockMvc.perform(put("/api/market-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedMarketOrder)))
                .andExpect(status().isOk());

        // Validate the MarketOrder in the database
        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeUpdate);
        MarketOrder testMarketOrder = marketOrders.get(marketOrders.size() - 1);
        assertThat(testMarketOrder.isBuy()).isEqualTo(UPDATED_BUY);
        assertThat(testMarketOrder.getIssued()).isEqualTo(UPDATED_ISSUED);
        assertThat(testMarketOrder.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testMarketOrder.getVolumeEntered()).isEqualTo(UPDATED_VOLUME_ENTERED);
        assertThat(testMarketOrder.getStationID()).isEqualTo(UPDATED_STATION_ID);
        assertThat(testMarketOrder.getVolume()).isEqualTo(UPDATED_VOLUME);
        assertThat(testMarketOrder.getRange()).isEqualTo(UPDATED_RANGE);
        assertThat(testMarketOrder.getMinVolume()).isEqualTo(UPDATED_MIN_VOLUME);
        assertThat(testMarketOrder.getDuration()).isEqualTo(UPDATED_DURATION);

        // Validate the MarketOrder in ElasticSearch
        MarketOrder marketOrderEs = marketOrderSearchRepository.findOne(testMarketOrder.getId());
        assertThat(marketOrderEs).isEqualToComparingFieldByField(testMarketOrder);
    }

    @Test
    @Transactional
    public void deleteMarketOrder() throws Exception {
        // Initialize the database
        marketOrderRepository.saveAndFlush(marketOrder);
        marketOrderSearchRepository.save(marketOrder);
        int databaseSizeBeforeDelete = marketOrderRepository.findAll().size();

        // Get the marketOrder
        restMarketOrderMockMvc.perform(delete("/api/market-orders/{id}", marketOrder.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean marketOrderExistsInEs = marketOrderSearchRepository.exists(marketOrder.getId());
        assertThat(marketOrderExistsInEs).isFalse();

        // Validate the database is empty
        List<MarketOrder> marketOrders = marketOrderRepository.findAll();
        assertThat(marketOrders).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchMarketOrder() throws Exception {
        // Initialize the database
        marketOrderRepository.saveAndFlush(marketOrder);
        marketOrderSearchRepository.save(marketOrder);

        // Search the marketOrder
        restMarketOrderMockMvc.perform(get("/api/_search/market-orders?query=id:" + marketOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(marketOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].buy").value(hasItem(DEFAULT_BUY.booleanValue())))
            .andExpect(jsonPath("$.[*].issued").value(hasItem(DEFAULT_ISSUED_STR)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].volumeEntered").value(hasItem(DEFAULT_VOLUME_ENTERED.doubleValue())))
            .andExpect(jsonPath("$.[*].stationID").value(hasItem(DEFAULT_STATION_ID.intValue())))
            .andExpect(jsonPath("$.[*].volume").value(hasItem(DEFAULT_VOLUME.intValue())))
            .andExpect(jsonPath("$.[*].range").value(hasItem(DEFAULT_RANGE.toString())))
            .andExpect(jsonPath("$.[*].minVolume").value(hasItem(DEFAULT_MIN_VOLUME.doubleValue())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)));
    }
}

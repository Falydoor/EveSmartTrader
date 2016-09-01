package com.smarttrader.web.rest;

import com.smarttrader.EveSmartTraderApp;
import com.smarttrader.domain.InvType;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.search.InvTypeSearchRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the InvTypeResource REST controller.
 *
 * @see InvTypeResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EveSmartTraderApp.class)
@WebAppConfiguration
@IntegrationTest
public class InvTypeResourceIntTest {


    private static final Long DEFAULT_GROUP_ID = 1L;
    private static final Long UPDATED_GROUP_ID = 2L;
    private static final String DEFAULT_TYPE_NAME = "AAAAA";
    private static final String UPDATED_TYPE_NAME = "BBBBB";
    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";

    private static final Double DEFAULT_MASS = 1D;
    private static final Double UPDATED_MASS = 2D;

    private static final Double DEFAULT_VOLUME = 1D;
    private static final Double UPDATED_VOLUME = 2D;

    private static final Double DEFAULT_CAPACITY = 1D;
    private static final Double UPDATED_CAPACITY = 2D;

    private static final Long DEFAULT_PORTION_SIZE = 1L;
    private static final Long UPDATED_PORTION_SIZE = 2L;

    private static final Long DEFAULT_RACE_ID = 1L;
    private static final Long UPDATED_RACE_ID = 2L;

    private static final Double DEFAULT_BASE_PRICE = 1D;
    private static final Double UPDATED_BASE_PRICE = 2D;

    private static final Integer DEFAULT_PUBLISHED = 1;
    private static final Integer UPDATED_PUBLISHED = 2;

    private static final Long DEFAULT_ICON_ID = 1L;
    private static final Long UPDATED_ICON_ID = 2L;

    private static final Long DEFAULT_SOUND_ID = 1L;
    private static final Long UPDATED_SOUND_ID = 2L;

    private static final Long DEFAULT_GRAPHIC_ID = 1L;
    private static final Long UPDATED_GRAPHIC_ID = 2L;

    @Inject
    private InvTypeRepository invTypeRepository;

    @Inject
    private InvTypeSearchRepository invTypeSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restInvTypeMockMvc;

    private InvType invType;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        InvTypeResource invTypeResource = new InvTypeResource();
        ReflectionTestUtils.setField(invTypeResource, "invTypeSearchRepository", invTypeSearchRepository);
        ReflectionTestUtils.setField(invTypeResource, "invTypeRepository", invTypeRepository);
        this.restInvTypeMockMvc = MockMvcBuilders.standaloneSetup(invTypeResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        invTypeSearchRepository.deleteAll();
        invType = new InvType();
        invType.setId(1L);
        invType.setGroupID(DEFAULT_GROUP_ID);
        invType.setTypeName(DEFAULT_TYPE_NAME);
        invType.setDescription(DEFAULT_DESCRIPTION);
        invType.setMass(DEFAULT_MASS);
        invType.setVolume(DEFAULT_VOLUME);
        invType.setCapacity(DEFAULT_CAPACITY);
        invType.setPortionSize(DEFAULT_PORTION_SIZE);
        invType.setRaceID(DEFAULT_RACE_ID);
        invType.setBasePrice(DEFAULT_BASE_PRICE);
        invType.setPublished(DEFAULT_PUBLISHED);
        invType.setIconID(DEFAULT_ICON_ID);
        invType.setSoundID(DEFAULT_SOUND_ID);
        invType.setGraphicID(DEFAULT_GRAPHIC_ID);
    }

    @Test
    @Transactional
    public void createInvType() throws Exception {
        int databaseSizeBeforeCreate = invTypeRepository.findAll().size();

        // Create the InvType

        restInvTypeMockMvc.perform(post("/api/inv-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invType)))
                .andExpect(status().isCreated());

        // Validate the InvType in the database
        List<InvType> invTypes = invTypeRepository.findAll();
        assertThat(invTypes).hasSize(databaseSizeBeforeCreate + 1);
        InvType testInvType = invTypes.get(invTypes.size() - 1);
        assertThat(testInvType.getGroupID()).isEqualTo(DEFAULT_GROUP_ID);
        assertThat(testInvType.getTypeName()).isEqualTo(DEFAULT_TYPE_NAME);
        assertThat(testInvType.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testInvType.getMass()).isEqualTo(DEFAULT_MASS);
        assertThat(testInvType.getVolume()).isEqualTo(DEFAULT_VOLUME);
        assertThat(testInvType.getCapacity()).isEqualTo(DEFAULT_CAPACITY);
        assertThat(testInvType.getPortionSize()).isEqualTo(DEFAULT_PORTION_SIZE);
        assertThat(testInvType.getRaceID()).isEqualTo(DEFAULT_RACE_ID);
        assertThat(testInvType.getBasePrice()).isEqualTo(DEFAULT_BASE_PRICE);
        assertThat(testInvType.getPublished()).isEqualTo(DEFAULT_PUBLISHED);
        assertThat(testInvType.getIconID()).isEqualTo(DEFAULT_ICON_ID);
        assertThat(testInvType.getSoundID()).isEqualTo(DEFAULT_SOUND_ID);
        assertThat(testInvType.getGraphicID()).isEqualTo(DEFAULT_GRAPHIC_ID);

        // Validate the InvType in ElasticSearch
        InvType invTypeEs = invTypeSearchRepository.findOne(testInvType.getId());
        assertThat(invTypeEs).isEqualToComparingFieldByField(testInvType);
    }

    @Test
    @Transactional
    public void getAllInvTypes() throws Exception {
        // Initialize the database
        invTypeRepository.saveAndFlush(invType);

        // Get all the invTypes
        restInvTypeMockMvc.perform(get("/api/inv-types?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(invType.getId().intValue())))
                .andExpect(jsonPath("$.[*].groupID").value(hasItem(DEFAULT_GROUP_ID.intValue())))
                .andExpect(jsonPath("$.[*].typeName").value(hasItem(DEFAULT_TYPE_NAME.toString())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
                .andExpect(jsonPath("$.[*].mass").value(hasItem(DEFAULT_MASS.doubleValue())))
                .andExpect(jsonPath("$.[*].volume").value(hasItem(DEFAULT_VOLUME.doubleValue())))
                .andExpect(jsonPath("$.[*].capacity").value(hasItem(DEFAULT_CAPACITY.doubleValue())))
                .andExpect(jsonPath("$.[*].portionSize").value(hasItem(DEFAULT_PORTION_SIZE.intValue())))
                .andExpect(jsonPath("$.[*].raceID").value(hasItem(DEFAULT_RACE_ID.intValue())))
                .andExpect(jsonPath("$.[*].basePrice").value(hasItem(DEFAULT_BASE_PRICE.doubleValue())))
                .andExpect(jsonPath("$.[*].published").value(hasItem(DEFAULT_PUBLISHED)))
                .andExpect(jsonPath("$.[*].iconID").value(hasItem(DEFAULT_ICON_ID.intValue())))
                .andExpect(jsonPath("$.[*].soundID").value(hasItem(DEFAULT_SOUND_ID.intValue())))
                .andExpect(jsonPath("$.[*].graphicID").value(hasItem(DEFAULT_GRAPHIC_ID.intValue())));
    }

    @Test
    @Transactional
    public void getInvType() throws Exception {
        // Initialize the database
        invTypeRepository.saveAndFlush(invType);

        // Get the invType
        restInvTypeMockMvc.perform(get("/api/inv-types/{id}", invType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(invType.getId().intValue()))
            .andExpect(jsonPath("$.groupID").value(DEFAULT_GROUP_ID.intValue()))
            .andExpect(jsonPath("$.typeName").value(DEFAULT_TYPE_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.mass").value(DEFAULT_MASS.doubleValue()))
            .andExpect(jsonPath("$.volume").value(DEFAULT_VOLUME.doubleValue()))
            .andExpect(jsonPath("$.capacity").value(DEFAULT_CAPACITY.doubleValue()))
            .andExpect(jsonPath("$.portionSize").value(DEFAULT_PORTION_SIZE.intValue()))
            .andExpect(jsonPath("$.raceID").value(DEFAULT_RACE_ID.intValue()))
            .andExpect(jsonPath("$.basePrice").value(DEFAULT_BASE_PRICE.doubleValue()))
            .andExpect(jsonPath("$.published").value(DEFAULT_PUBLISHED))
            .andExpect(jsonPath("$.iconID").value(DEFAULT_ICON_ID.intValue()))
            .andExpect(jsonPath("$.soundID").value(DEFAULT_SOUND_ID.intValue()))
            .andExpect(jsonPath("$.graphicID").value(DEFAULT_GRAPHIC_ID.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingInvType() throws Exception {
        // Get the invType
        restInvTypeMockMvc.perform(get("/api/inv-types/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateInvType() throws Exception {
        // Initialize the database
        invTypeRepository.saveAndFlush(invType);
        invTypeSearchRepository.save(invType);
        int databaseSizeBeforeUpdate = invTypeRepository.findAll().size();

        // Update the invType
        InvType updatedInvType = new InvType();
        updatedInvType.setId(invType.getId());
        updatedInvType.setGroupID(UPDATED_GROUP_ID);
        updatedInvType.setTypeName(UPDATED_TYPE_NAME);
        updatedInvType.setDescription(UPDATED_DESCRIPTION);
        updatedInvType.setMass(UPDATED_MASS);
        updatedInvType.setVolume(UPDATED_VOLUME);
        updatedInvType.setCapacity(UPDATED_CAPACITY);
        updatedInvType.setPortionSize(UPDATED_PORTION_SIZE);
        updatedInvType.setRaceID(UPDATED_RACE_ID);
        updatedInvType.setBasePrice(UPDATED_BASE_PRICE);
        updatedInvType.setPublished(UPDATED_PUBLISHED);
        updatedInvType.setIconID(UPDATED_ICON_ID);
        updatedInvType.setSoundID(UPDATED_SOUND_ID);
        updatedInvType.setGraphicID(UPDATED_GRAPHIC_ID);

        restInvTypeMockMvc.perform(put("/api/inv-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedInvType)))
                .andExpect(status().isOk());

        // Validate the InvType in the database
        List<InvType> invTypes = invTypeRepository.findAll();
        assertThat(invTypes).hasSize(databaseSizeBeforeUpdate);
        InvType testInvType = invTypes.get(invTypes.size() - 1);
        assertThat(testInvType.getGroupID()).isEqualTo(UPDATED_GROUP_ID);
        assertThat(testInvType.getTypeName()).isEqualTo(UPDATED_TYPE_NAME);
        assertThat(testInvType.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testInvType.getMass()).isEqualTo(UPDATED_MASS);
        assertThat(testInvType.getVolume()).isEqualTo(UPDATED_VOLUME);
        assertThat(testInvType.getCapacity()).isEqualTo(UPDATED_CAPACITY);
        assertThat(testInvType.getPortionSize()).isEqualTo(UPDATED_PORTION_SIZE);
        assertThat(testInvType.getRaceID()).isEqualTo(UPDATED_RACE_ID);
        assertThat(testInvType.getBasePrice()).isEqualTo(UPDATED_BASE_PRICE);
        assertThat(testInvType.getPublished()).isEqualTo(UPDATED_PUBLISHED);
        assertThat(testInvType.getIconID()).isEqualTo(UPDATED_ICON_ID);
        assertThat(testInvType.getSoundID()).isEqualTo(UPDATED_SOUND_ID);
        assertThat(testInvType.getGraphicID()).isEqualTo(UPDATED_GRAPHIC_ID);

        // Validate the InvType in ElasticSearch
        InvType invTypeEs = invTypeSearchRepository.findOne(testInvType.getId());
        assertThat(invTypeEs).isEqualToComparingFieldByField(testInvType);
    }

    @Test
    @Transactional
    public void deleteInvType() throws Exception {
        // Initialize the database
        invTypeRepository.saveAndFlush(invType);
        invTypeSearchRepository.save(invType);
        int databaseSizeBeforeDelete = invTypeRepository.findAll().size();

        // Get the invType
        restInvTypeMockMvc.perform(delete("/api/inv-types/{id}", invType.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean invTypeExistsInEs = invTypeSearchRepository.exists(invType.getId());
        assertThat(invTypeExistsInEs).isFalse();

        // Validate the database is empty
        List<InvType> invTypes = invTypeRepository.findAll();
        assertThat(invTypes).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchInvType() throws Exception {
        // Initialize the database
        invTypeRepository.saveAndFlush(invType);
        invTypeSearchRepository.save(invType);

        // Search the invType
        restInvTypeMockMvc.perform(get("/api/_search/inv-types?query=id:" + invType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(invType.getId().intValue())))
            .andExpect(jsonPath("$.[*].groupID").value(hasItem(DEFAULT_GROUP_ID.intValue())))
            .andExpect(jsonPath("$.[*].typeName").value(hasItem(DEFAULT_TYPE_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].mass").value(hasItem(DEFAULT_MASS.doubleValue())))
            .andExpect(jsonPath("$.[*].volume").value(hasItem(DEFAULT_VOLUME.doubleValue())))
            .andExpect(jsonPath("$.[*].capacity").value(hasItem(DEFAULT_CAPACITY.doubleValue())))
            .andExpect(jsonPath("$.[*].portionSize").value(hasItem(DEFAULT_PORTION_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].raceID").value(hasItem(DEFAULT_RACE_ID.intValue())))
            .andExpect(jsonPath("$.[*].basePrice").value(hasItem(DEFAULT_BASE_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].published").value(hasItem(DEFAULT_PUBLISHED)))
            .andExpect(jsonPath("$.[*].iconID").value(hasItem(DEFAULT_ICON_ID.intValue())))
            .andExpect(jsonPath("$.[*].soundID").value(hasItem(DEFAULT_SOUND_ID.intValue())))
            .andExpect(jsonPath("$.[*].graphicID").value(hasItem(DEFAULT_GRAPHIC_ID.intValue())));
    }
}

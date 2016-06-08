package com.smarttrader.web.rest;

import com.smarttrader.EveSmartTraderApp;
import com.smarttrader.domain.InvMarketGroup;
import com.smarttrader.repository.InvMarketGroupRepository;
import com.smarttrader.repository.search.InvMarketGroupSearchRepository;

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
 * Test class for the InvMarketGroupResource REST controller.
 *
 * @see InvMarketGroupResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EveSmartTraderApp.class)
@WebAppConfiguration
@IntegrationTest
public class InvMarketGroupResourceIntTest {


    private static final Long DEFAULT_PARENT_GROUP_ID = 1L;
    private static final Long UPDATED_PARENT_GROUP_ID = 2L;
    private static final String DEFAULT_MARKET_GROUP_NAME = "AAAAA";
    private static final String UPDATED_MARKET_GROUP_NAME = "BBBBB";
    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";

    private static final Integer DEFAULT_ICON_ID = 1;
    private static final Integer UPDATED_ICON_ID = 2;

    private static final Integer DEFAULT_HAS_TYPES = 1;
    private static final Integer UPDATED_HAS_TYPES = 2;

    @Inject
    private InvMarketGroupRepository invMarketGroupRepository;

    @Inject
    private InvMarketGroupSearchRepository invMarketGroupSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restInvMarketGroupMockMvc;

    private InvMarketGroup invMarketGroup;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        InvMarketGroupResource invMarketGroupResource = new InvMarketGroupResource();
        ReflectionTestUtils.setField(invMarketGroupResource, "invMarketGroupSearchRepository", invMarketGroupSearchRepository);
        ReflectionTestUtils.setField(invMarketGroupResource, "invMarketGroupRepository", invMarketGroupRepository);
        this.restInvMarketGroupMockMvc = MockMvcBuilders.standaloneSetup(invMarketGroupResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        invMarketGroupSearchRepository.deleteAll();
        invMarketGroup = new InvMarketGroup();
        invMarketGroup.setParentGroupID(DEFAULT_PARENT_GROUP_ID);
        invMarketGroup.setMarketGroupName(DEFAULT_MARKET_GROUP_NAME);
        invMarketGroup.setDescription(DEFAULT_DESCRIPTION);
        invMarketGroup.setIconID(DEFAULT_ICON_ID);
        invMarketGroup.setHasTypes(DEFAULT_HAS_TYPES);
    }

    @Test
    @Transactional
    public void createInvMarketGroup() throws Exception {
        int databaseSizeBeforeCreate = invMarketGroupRepository.findAll().size();

        // Create the InvMarketGroup

        restInvMarketGroupMockMvc.perform(post("/api/inv-market-groups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invMarketGroup)))
                .andExpect(status().isCreated());

        // Validate the InvMarketGroup in the database
        List<InvMarketGroup> invMarketGroups = invMarketGroupRepository.findAll();
        assertThat(invMarketGroups).hasSize(databaseSizeBeforeCreate + 1);
        InvMarketGroup testInvMarketGroup = invMarketGroups.get(invMarketGroups.size() - 1);
        assertThat(testInvMarketGroup.getParentGroupID()).isEqualTo(DEFAULT_PARENT_GROUP_ID);
        assertThat(testInvMarketGroup.getMarketGroupName()).isEqualTo(DEFAULT_MARKET_GROUP_NAME);
        assertThat(testInvMarketGroup.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testInvMarketGroup.getIconID()).isEqualTo(DEFAULT_ICON_ID);
        assertThat(testInvMarketGroup.getHasTypes()).isEqualTo(DEFAULT_HAS_TYPES);

        // Validate the InvMarketGroup in ElasticSearch
        InvMarketGroup invMarketGroupEs = invMarketGroupSearchRepository.findOne(testInvMarketGroup.getId());
        assertThat(invMarketGroupEs).isEqualToComparingFieldByField(testInvMarketGroup);
    }

    @Test
    @Transactional
    public void getAllInvMarketGroups() throws Exception {
        // Initialize the database
        invMarketGroupRepository.saveAndFlush(invMarketGroup);

        // Get all the invMarketGroups
        restInvMarketGroupMockMvc.perform(get("/api/inv-market-groups?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(invMarketGroup.getId().intValue())))
                .andExpect(jsonPath("$.[*].parentGroupID").value(hasItem(DEFAULT_PARENT_GROUP_ID.intValue())))
                .andExpect(jsonPath("$.[*].marketGroupName").value(hasItem(DEFAULT_MARKET_GROUP_NAME.toString())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
                .andExpect(jsonPath("$.[*].iconID").value(hasItem(DEFAULT_ICON_ID)))
                .andExpect(jsonPath("$.[*].hasTypes").value(hasItem(DEFAULT_HAS_TYPES)));
    }

    @Test
    @Transactional
    public void getInvMarketGroup() throws Exception {
        // Initialize the database
        invMarketGroupRepository.saveAndFlush(invMarketGroup);

        // Get the invMarketGroup
        restInvMarketGroupMockMvc.perform(get("/api/inv-market-groups/{id}", invMarketGroup.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(invMarketGroup.getId().intValue()))
            .andExpect(jsonPath("$.parentGroupID").value(DEFAULT_PARENT_GROUP_ID.intValue()))
            .andExpect(jsonPath("$.marketGroupName").value(DEFAULT_MARKET_GROUP_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.iconID").value(DEFAULT_ICON_ID))
            .andExpect(jsonPath("$.hasTypes").value(DEFAULT_HAS_TYPES));
    }

    @Test
    @Transactional
    public void getNonExistingInvMarketGroup() throws Exception {
        // Get the invMarketGroup
        restInvMarketGroupMockMvc.perform(get("/api/inv-market-groups/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateInvMarketGroup() throws Exception {
        // Initialize the database
        invMarketGroupRepository.saveAndFlush(invMarketGroup);
        invMarketGroupSearchRepository.save(invMarketGroup);
        int databaseSizeBeforeUpdate = invMarketGroupRepository.findAll().size();

        // Update the invMarketGroup
        InvMarketGroup updatedInvMarketGroup = new InvMarketGroup();
        updatedInvMarketGroup.setId(invMarketGroup.getId());
        updatedInvMarketGroup.setParentGroupID(UPDATED_PARENT_GROUP_ID);
        updatedInvMarketGroup.setMarketGroupName(UPDATED_MARKET_GROUP_NAME);
        updatedInvMarketGroup.setDescription(UPDATED_DESCRIPTION);
        updatedInvMarketGroup.setIconID(UPDATED_ICON_ID);
        updatedInvMarketGroup.setHasTypes(UPDATED_HAS_TYPES);

        restInvMarketGroupMockMvc.perform(put("/api/inv-market-groups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedInvMarketGroup)))
                .andExpect(status().isOk());

        // Validate the InvMarketGroup in the database
        List<InvMarketGroup> invMarketGroups = invMarketGroupRepository.findAll();
        assertThat(invMarketGroups).hasSize(databaseSizeBeforeUpdate);
        InvMarketGroup testInvMarketGroup = invMarketGroups.get(invMarketGroups.size() - 1);
        assertThat(testInvMarketGroup.getParentGroupID()).isEqualTo(UPDATED_PARENT_GROUP_ID);
        assertThat(testInvMarketGroup.getMarketGroupName()).isEqualTo(UPDATED_MARKET_GROUP_NAME);
        assertThat(testInvMarketGroup.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testInvMarketGroup.getIconID()).isEqualTo(UPDATED_ICON_ID);
        assertThat(testInvMarketGroup.getHasTypes()).isEqualTo(UPDATED_HAS_TYPES);

        // Validate the InvMarketGroup in ElasticSearch
        InvMarketGroup invMarketGroupEs = invMarketGroupSearchRepository.findOne(testInvMarketGroup.getId());
        assertThat(invMarketGroupEs).isEqualToComparingFieldByField(testInvMarketGroup);
    }

    @Test
    @Transactional
    public void deleteInvMarketGroup() throws Exception {
        // Initialize the database
        invMarketGroupRepository.saveAndFlush(invMarketGroup);
        invMarketGroupSearchRepository.save(invMarketGroup);
        int databaseSizeBeforeDelete = invMarketGroupRepository.findAll().size();

        // Get the invMarketGroup
        restInvMarketGroupMockMvc.perform(delete("/api/inv-market-groups/{id}", invMarketGroup.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean invMarketGroupExistsInEs = invMarketGroupSearchRepository.exists(invMarketGroup.getId());
        assertThat(invMarketGroupExistsInEs).isFalse();

        // Validate the database is empty
        List<InvMarketGroup> invMarketGroups = invMarketGroupRepository.findAll();
        assertThat(invMarketGroups).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchInvMarketGroup() throws Exception {
        // Initialize the database
        invMarketGroupRepository.saveAndFlush(invMarketGroup);
        invMarketGroupSearchRepository.save(invMarketGroup);

        // Search the invMarketGroup
        restInvMarketGroupMockMvc.perform(get("/api/_search/inv-market-groups?query=id:" + invMarketGroup.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(invMarketGroup.getId().intValue())))
            .andExpect(jsonPath("$.[*].parentGroupID").value(hasItem(DEFAULT_PARENT_GROUP_ID.intValue())))
            .andExpect(jsonPath("$.[*].marketGroupName").value(hasItem(DEFAULT_MARKET_GROUP_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].iconID").value(hasItem(DEFAULT_ICON_ID)))
            .andExpect(jsonPath("$.[*].hasTypes").value(hasItem(DEFAULT_HAS_TYPES)));
    }
}

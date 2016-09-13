package com.smarttrader.service;

import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.model.shared.MarketOrder;
import com.beimin.eveapi.parser.ApiAuthorization;
import com.beimin.eveapi.parser.pilot.MarketOrdersParser;
import com.beimin.eveapi.response.shared.MarketOrdersResponse;
import com.smarttrader.domain.User;
import com.smarttrader.security.SecurityUtils;
import com.smarttrader.web.rest.dto.UserMarketDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for eve api.
 */
@Service
@Transactional
public class EveApiService {

    private final Logger log = LoggerFactory.getLogger(EveApiService.class);

    @Inject
    private UserService userService;

    public UserMarketDTO getUserMarketOrders() {
        User user = userService.getUserWithAuthorities();
        if (user.getKeyId() == null || StringUtils.isBlank(user.getVCode())) {
            return new UserMarketDTO();
        }
        try {
            MarketOrdersParser parser = new MarketOrdersParser();
            ApiAuthorization auth = new ApiAuthorization(user.getKeyId(), user.getVCode());
            MarketOrdersResponse response = parser.getResponse(auth);
            Map<Integer, Set<Integer>> typeIDByBid = response.getAll().stream()
                .filter(this::isValidMarketOrderFromStation)
                .collect(Collectors.groupingBy(MarketOrder::getBid, Collectors.mapping(MarketOrder::getTypeID, Collectors.toSet())));
            return new UserMarketDTO(typeIDByBid);
        } catch (ApiException e) {
            log.error("Unable to retrieve user's market orders", e);
        }
        return new UserMarketDTO();
    }

    private boolean isValidMarketOrderFromStation(MarketOrder marketOrder) {
        return SecurityUtils.getBuyId() == marketOrder.getStationID() && marketOrder.getOrderState() == 0;
    }

}

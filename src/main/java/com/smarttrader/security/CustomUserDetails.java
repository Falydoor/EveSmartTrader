package com.smarttrader.security;

import com.smarttrader.domain.enums.Station;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Created by Theo on 9/2/16.
 */
public class CustomUserDetails extends User {

    private Station station;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Station station) {
        super(username, password, authorities);
        this.station = station;
    }

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public CustomUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }
}

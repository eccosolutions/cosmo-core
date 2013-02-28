/*
 * Copyright 2008 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.acegisecurity.providers.wsse;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.osaf.cosmo.wsse.UsernameToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
   AuthenticationToken that contains a WSSE Username token.
 */
public class WsseAuthenticationToken extends AbstractAuthenticationToken
    implements Serializable {

    private static final Collection<GrantedAuthority> NO_AUTHORITIES = Collections.emptyList();

    private boolean authenticated = false;
    private UserDetails userDetails = null;
    private UsernameToken token = null;


    public WsseAuthenticationToken(UsernameToken token) {
        super(NO_AUTHORITIES);
        if (token == null)
            throw new IllegalArgumentException("token may not be null");
        this.token = token;
    }

    // Authentication methods

    /** */
    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        authenticated = isAuthenticated;
    }

    /** */
    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * return token
     */
    @Override
    public Object getCredentials() {
        return token;
    }

    /**
     * Returns the userDetails.
     */
    @Override
    public Object getPrincipal() {
        return userDetails;
    }

    // our methods

    /** */
    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        if(userDetails==null)
            return NO_AUTHORITIES;
        else
            return (Collection<GrantedAuthority>) userDetails.getAuthorities();
    }

    /** */
    @Override
    public boolean equals(Object obj) {
        if (! super.equals(obj)) {
            return false;
        }
        if (! (obj instanceof WsseAuthenticationToken)) {
            return false;
        }
        WsseAuthenticationToken test = (WsseAuthenticationToken) obj;
        return token.equals(test.getCredentials());
    }
}

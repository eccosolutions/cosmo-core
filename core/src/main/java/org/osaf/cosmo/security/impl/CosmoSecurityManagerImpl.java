/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.security.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.PermissionDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

/**
 * The default implementation of the {@link CosmoSecurityManager}
 * interface that provides a {@link CosmoSecurityContext} from
 * security information contained in JAAS or Acegi Security.
 */
public class CosmoSecurityManagerImpl implements CosmoSecurityManager {
    private static final Log log =
        LogFactory.getLog(CosmoSecurityManagerImpl.class);

    private AuthenticationManager authenticationManager;

    // store additional tickets for authenticated principal
    private final ThreadLocal<Set<Ticket>> tickets = new ThreadLocal<Set<Ticket>>();

    /* ----- CosmoSecurityManager methods ----- */

    /**
     * Provide a <code>CosmoSecurityContext</code> representing a
     * Cosmo user previously authenticated by the Cosmo security
     * system.
     */
    @Override
    public CosmoSecurityContext getSecurityContext()
        throws CosmoSecurityException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authen = context.getAuthentication();
        if (authen == null) {
            throw new CosmoSecurityException("no Authentication found in " +
                                             "SecurityContext");
        }

        return createSecurityContext(authen);
    }

    /**
     * Authenticate the given Cosmo credentials and register a
     * <code>CosmoSecurityContext</code> for them. This method is used
     * when Cosmo components need to programatically log in a user
     * rather than relying on a security context already being in
     * place.
     */
    @Override
    public CosmoSecurityContext initiateSecurityContext(String username,
                                                        String password)
        throws CosmoSecurityException {
        try {
            UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication =
                authenticationManager.authenticate(credentials);
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authentication);
            return createSecurityContext(authentication);
        } catch (AuthenticationException e) {
            throw new CosmoSecurityException("can't establish security context",
                                             e);
        }
    }

    /**
     * Initiate the current security context with the current user.
     * This method is used when the server needs to run code as a
     * specific user.
     */
    @Override
    public CosmoSecurityContext initiateSecurityContext(User user)
    	throws CosmoSecurityException {

    	UserDetails details = new CosmoUserDetails(user);

    	UsernamePasswordAuthenticationToken credentials =
            new UsernamePasswordAuthenticationToken(details, "", details.getAuthorities());

    	credentials.setDetails(details);
    	SecurityContext sc = SecurityContextHolder.getContext();
    	sc.setAuthentication(credentials);
    	return createSecurityContext(credentials);
    }

    /**
     * Validates that the current security context has the requested
     * permission for the given item.
     *
     * @throws PermissionDeniedException if the security context does
     * not have the required permission
     */
    @Override
    public void checkPermission(Item item,
                                int permission)
        throws PermissionDeniedException, CosmoSecurityException {
        CosmoSecurityContext ctx = getSecurityContext();

        if (ctx.isAnonymous()) {
            log.warn("Anonymous access attempted to item " + item.getUid());
            throw new PermissionDeniedException("Anonymous principals have no permissions");
        }

        // administrators can do anything to any item
        if (ctx.isAdmin())
            return;

        User user = ctx.getUser();
        if (user != null) {
            // an item's owner can do anything to an item he owns
            if (user.equals(item.getOwner()))
                return;
            log.warn("User " + user.getUsername() + " attempted access to item " + item.getUid() + " owned by " + item.getOwner().getUsername());
            throw new PermissionDeniedException("User does not have appropriate permissions on item " + item.getUid());
        }
        throw new PermissionDeniedException("No user!");
    }

    /* ----- our methods ----- */

    /**
     */
    protected CosmoSecurityContext
        createSecurityContext(Authentication authen) {
        return new CosmoSecurityContextImpl(authen, tickets.get());
    }

    /**
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     */
    public void
        setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void registerTickets(Set<Ticket> tickets) {
        Set<Ticket> currentTickets = this.tickets.get();
        if(currentTickets==null) {
            this.tickets.set(new HashSet<Ticket>());
        }
        this.tickets.get().addAll(tickets);
    }

    @Override
    public void unregisterTickets() {
        this.tickets.remove();
    }
}

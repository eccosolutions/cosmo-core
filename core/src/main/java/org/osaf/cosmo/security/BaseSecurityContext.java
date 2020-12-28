/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.security;

import java.security.Principal;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 * Base class for implementations of {@link CosmoSecurityContext}.
 */
public abstract class BaseSecurityContext implements CosmoSecurityContext {
    private static final Log log =
        LogFactory.getLog(BaseSecurityContext.class);

    private boolean admin;
    private boolean anonymous;
    private Principal principal;
    private User user;
    private Set<Ticket> tickets;

    /**
     */
    public BaseSecurityContext(Principal principal, Set<Ticket> tickets) {
        this.anonymous = false;
        this.principal = principal;
        this.admin = false;
        this.tickets = tickets;
        processPrincipal();
    }

    /* ----- CosmoSecurityContext methods ----- */

    /**
     * Returns a name describing the principal for this security
     * context (the name of the Cosmo user, the id of the ticket, or
     * the string <code>anonymous</code>.
     */
    public String getName() {
        if (isAnonymous()) {
            return "anonymous";
        }
        return user.getUsername();
    }

    /**
     * Determines whether or not the context represents an anonymous
     * Cosmo user.
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Returns an instance of {@link User} describing the user
     * represented by the security context, or <code>null</code> if
     * the context does not represent a user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Determines whether or not the security context represents an
     * administrator
     */
    public boolean isAdmin() {
        return admin;
    }


    /* ----- our methods ----- */

    protected Principal getPrincipal() {
        return principal;
    }

    protected void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    protected void setUser(User user) {
        this.user = user;
    }

    protected void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * Called by the constructor to set the context state. Examines
     * the principal to decide if it represents a ticket or user or
     * anonymous access.
     */
    protected abstract void processPrincipal();

    public String toString() {
        return ToStringBuilder.
            reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }
}

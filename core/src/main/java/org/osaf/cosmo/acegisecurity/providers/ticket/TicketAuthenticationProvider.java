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
package org.osaf.cosmo.acegisecurity.providers.ticket;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.AuthenticationProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.server.CollectionPath;
import org.osaf.cosmo.server.ItemPath;
import org.springframework.dao.DataAccessException;

/**
 */
public class TicketAuthenticationProvider
    implements AuthenticationProvider {
    private static final Log log =
        LogFactory.getLog(TicketAuthenticationProvider.class);

    private ContentDao contentDao;

    // AuthenticationProvider methods

    /** */
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        if (! supports(authentication.getClass()))
            return null;

        TicketAuthenticationToken token =
            (TicketAuthenticationToken) authentication;
        for (String key : token.getKeys()) {
            Ticket ticket = findTicket(token.getPath(), key);
            if (ticket != null) {
                token.setTicket(ticket);
                token.setAuthenticated(true);
                return token;
            }
        }

        throw new BadCredentialsException("No valid tickets found for resource at " + token.getPath());
    }

    /** */
    public boolean supports(Class authentication) {
        return TicketAuthenticationToken.class.
            isAssignableFrom(authentication);
    }

    // our methods

    /** */
    public ContentDao getContentDao() {
        return contentDao;
    }

    /** */
    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    private Ticket findTicket(String path,
                              String key) {
        try {
            if (log.isDebugEnabled())
                log.debug("authenticating ticket " + key +
                          " for resource at path " + path);

            Item item = findItem(path);
            Ticket ticket = contentDao.getTicket(item, key);
            if (ticket == null)
                return null;

            if (ticket.hasTimedOut()) {
                if (log.isDebugEnabled())
                    log.debug("removing timed out ticket " + ticket.getKey());
                contentDao.removeTicket(item, ticket);
                return null;
            }

            return ticket;
        } catch (DataAccessException e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    private Item findItem(String path) {
        CollectionPath cp = CollectionPath.parse(path, true);
        Item item = null;

        if (cp != null) {
            item = contentDao.findItemByUid(cp.getUid());
            // a ticket cannot be used to access a collection that
            // does not already exist
            if (item == null)
                throw new TicketedItemNotFoundException("Item with uid " + cp.getUid() + " not found");

            return item;
        }

        ItemPath ip = ItemPath.parse(path, true);
        if (ip != null) {
            item = contentDao.findItemByUid(ip.getUid());
            if (item != null)
                return item;
            else
                log.debug("no item found for uid: " + ip.getUid());
        }
        
        item = contentDao.findItemByPath(path);
        if (item == null)
            // if the item's parent exists, the ticket may be good for
            // that
            item = contentDao.findItemParentByPath(path);
        if (item == null) {
            log.debug("no item found for path: " + path);
            throw new TicketedItemNotFoundException("Resource at " + path + " not found");
        }

        return item;
    }
}

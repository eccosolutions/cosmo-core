/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.impl;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;

import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;

/**
 * Extends <code>DavCollection</code> to adapt the Cosmo
 * <code>HomeCollectionItem</code> to the DAV resource model.
 *
 * @see DavCollection
 * @see HomeCollectionItem
 */
public class DavHomeCollection extends DavCollectionBase {
    private static final Log log =
        LogFactory.getLog(DavHomeCollection.class);

    /** */
    public DavHomeCollection(HomeCollectionItem collection,
                             DavResourceLocator locator,
                             DavResourceFactory factory,
                             EntityFactory entityFactory)
        throws DavException {
        super(collection, locator, factory, entityFactory);
    }

    // DavResource

    /** */
    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, MKTICKET, DELTICKET";
    }

    // DavCollection

    public boolean isHomeCollection() {
        return true;
    }

	@Override
	public DavResourceIterator getMembers() {
		ArrayList<DavResource> members = new ArrayList<DavResource>();
		try {
			for (Item memberItem : ((CollectionItem) getItem()).getChildren()) {
				DavResource resource = memberToResource(memberItem);
				if (resource != null)
					members.add(resource);
			}
			
			// for now scheduling is an option
			if(isSchedulingEnabled()) {
			    members.add(memberToResource(TEMPLATE_USER_INBOX.bindAbsolute(getResourceLocator().getBaseHref(), getResourcePath())));
			    members.add(memberToResource(TEMPLATE_USER_OUTBOX.bindAbsolute(getResourceLocator().getBaseHref(), getResourcePath())));
			}
			
			if (log.isTraceEnabled()) {
				log.trace("Members of Home Collection: " + members);
			}
			return new DavResourceIteratorImpl(members);
		} catch (DavException e) {
			throw new RuntimeException(e);
		}
	}
}

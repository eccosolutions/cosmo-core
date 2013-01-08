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
package org.osaf.cosmo.dav.ticket;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * Provides constants for request headers, URL parameters, and XML
 * namespaces, elements and values, DAV properties and resource types
 * defined by the WebDAV ticket spec.
 */
public interface TicketConstants extends DavConstants {

    /** The HTTP header name <code>Ticket</code> */
    public static final String HEADER_TICKET = "Ticket";

    /** The URL query string parameter name <code>ticket</code> */
    public static final String PARAM_TICKET = "ticket";

    /** The ticket XML namespace  */
    public static final Namespace NAMESPACE_TICKET =
        Namespace.getNamespace("ticket", "http://www.xythos.com/namespaces/StorageServer");

    /** The ticket XML element name <ticket:ticketinfo  */
    public static final String ELEMENT_TICKET_TICKETINFO = "ticketinfo";
    public static final String QN_TICKET_TICKETINFO =
        DomUtil.getQualifiedName(ELEMENT_TICKET_TICKETINFO, NAMESPACE_TICKET);
    /** The ticket XML element name <ticket:id  */
    public static final String ELEMENT_TICKET_ID = "id";
    public static final String QN_TICKET_ID =
        DomUtil.getQualifiedName(ELEMENT_TICKET_ID, NAMESPACE_TICKET);
    /** The ticket XML element name <ticket:timeout  */
    public static final String ELEMENT_TICKET_TIMEOUT = "timeout";
    public static final String QN_TICKET_TIMEOUT =
        DomUtil.getQualifiedName(ELEMENT_TICKET_TIMEOUT, NAMESPACE_TICKET);
    /** The ticket XML element name <ticket:visits  */
    public static final String ELEMENT_TICKET_VISITS = "visits";
    /** The ticket XML element name <ticket:freebusy  */
    public static final String ELEMENT_TICKET_FREEBUSY = "freebusy";

    /** The XML value <code>infinity</code> */
    public static final String VALUE_INFINITY = "infinity";

    /** The DAV property name <code>ticketdiscovery</code> */
    public static final String PROPERTY_TICKET_TICKETDISCOVERY =
        "ticketdiscovery";

    /** The ticket property <code>ticket:ticketdiscovery</code> */
    public static final DavPropertyName TICKETDISCOVERY =
        DavPropertyName.create(PROPERTY_TICKET_TICKETDISCOVERY,
                               NAMESPACE_TICKET);
}

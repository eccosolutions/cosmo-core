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
package org.osaf.cosmo.dav.acl.resource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.ForbiddenException;
import org.osaf.cosmo.dav.ProtectedPropertyModificationException;
import org.osaf.cosmo.dav.acl.AclConstants;
import org.osaf.cosmo.dav.acl.DavAce;
import org.osaf.cosmo.dav.acl.DavAcl;
import org.osaf.cosmo.dav.acl.DavPrivilege;
import org.osaf.cosmo.dav.acl.property.AlternateUriSet;
import org.osaf.cosmo.dav.acl.property.GroupMembership;
import org.osaf.cosmo.dav.acl.property.PrincipalUrl;
import org.osaf.cosmo.dav.acl.report.PrincipalMatchReport;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.property.CalendarHomeSet;
import org.osaf.cosmo.dav.caldav.property.CalendarUserAddressSet;
import org.osaf.cosmo.dav.caldav.property.ScheduleInboxURL;
import org.osaf.cosmo.dav.caldav.property.ScheduleOutboxURL;
import org.osaf.cosmo.dav.impl.DavResourceBase;
import org.osaf.cosmo.dav.property.CreationDate;
import org.osaf.cosmo.dav.property.DavProperty;
import org.osaf.cosmo.dav.property.DisplayName;
import org.osaf.cosmo.dav.property.Etag;
import org.osaf.cosmo.dav.property.IsCollection;
import org.osaf.cosmo.dav.property.LastModified;
import org.osaf.cosmo.dav.property.ResourceType;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.xml.DomWriter;
import org.w3c.dom.Element;

/**
* <p>
* Models a WebDAV principal resource (as described in RFC 3744) that
* represents a user account.
* </p>
 *
 * @see DavContent
 * @see DavResourceBase
 * @see User
 */
public class DavUserPrincipal extends DavResourceBase
    implements AclConstants, CaldavConstants, DavContent {
    private static final Log log = LogFactory.getLog(DavUserPrincipal.class);    
    private static final Set<ReportType> REPORT_TYPES =
        new HashSet<ReportType>();

    static {
        registerLiveProperty(DavPropertyName.CREATIONDATE);
        registerLiveProperty(DavPropertyName.GETLASTMODIFIED);
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
        registerLiveProperty(DavPropertyName.GETETAG);
        registerLiveProperty(CALENDARHOMESET);
        registerLiveProperty(CALENDARUSERADDRESSSET);
        registerLiveProperty(SCHEDULEINBOXURL);
        registerLiveProperty(SCHEDULEOUTBOXURL);
        registerLiveProperty(ALTERNATEURISET);
        registerLiveProperty(PRINCIPALURL);
        registerLiveProperty(GROUPMEMBERSHIP);

        REPORT_TYPES.add(PrincipalMatchReport.REPORT_TYPE_PRINCIPAL_MATCH);
    }

    private User user;
    private DavUserPrincipalCollection parent;
    private DavAcl acl;

    public DavUserPrincipal(User user,
                            DavResourceLocator locator,
                            DavResourceFactory factory)
        throws DavException {
        super(locator, factory);
        this.user = user;
        this.acl = makeAcl();
    }


    // Jackrabbit DavResource

    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT";
    }

    public boolean isCollection() {
        return false;
    }

    public long getModificationTime() {
        return user.getModifiedDate().getTime();
    }

    public boolean exists() {
        return true;
    }

    public String getDisplayName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    public String getETag() {
        return "\"" + user.getEntityTag() + "\"";
    }

    public void writeTo(OutputContext context)
        throws DavException, IOException {
        writeHtmlRepresentation(context);
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member,
                          InputContext inputContext)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public DavResourceIterator getMembers() {
        // while it would be ideal to throw an UnsupportedOperationException,
        // MultiStatus tries to add a MultiStatusResponse for every member
        // of a DavResource regardless of whether or not it's a collection,
        // so we need to return an empty iterator.
        return new DavResourceIteratorImpl(new ArrayList());
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResource getCollection() {
        try {
            return getParent();
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
    }

    public void move(org.apache.jackrabbit.webdav.DavResource destination)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public void copy(org.apache.jackrabbit.webdav.DavResource destination,
                     boolean shallow)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    // DavResource methods

    public DavCollection getParent()
        throws DavException {
        if (parent == null) {
            DavResourceLocator parentLocator =
                getResourceLocator().getParentLocator();
            parent = (DavUserPrincipalCollection)
                getResourceFactory().resolve(parentLocator);
        }

        return parent;
    }

    // our methods

    public User getUser() {
        return user;
    }

    protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<QName>(1);
        rt.add(RESOURCE_TYPE_PRINCIPAL);
        return rt;
    }

    public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }
    
    /**
     * Returns the resource's access control list. The list contains the
     * following ACEs:
     *
     * <ol>
     * <li> <code>DAV:unauthenticated</code>: deny <code>DAV:all</code> </li>
     * <li> <code>DAV:owner</code>: allow <code>DAV:all</code> </li>
     * <li> <code>DAV:all</code>: allow
     * <code>DAV:read-current-user-privilege-set</code> </li>
     * <li> <code>DAV:all</code>: deny <code>DAV:all</code> </li>
     * </ol>
     *
     * <p>
     * TODO: Include administrative users in the ACL, probably with a group
     * principal.
     * </p>
     */
    protected DavAcl getAcl() {
        return acl;
    }

    private DavAcl makeAcl() {
        DavAcl acl = new DavAcl();

        DavAce unauthenticated = new DavAce.UnauthenticatedAce();
        unauthenticated.setDenied(true);
        unauthenticated.getPrivileges().add(DavPrivilege.ALL);
        unauthenticated.setProtected(true);
        acl.getAces().add(unauthenticated);

        DavAce owner = new DavAce.SelfAce();
        owner.getPrivileges().add(DavPrivilege.ALL);
        owner.setProtected(true);
        acl.getAces().add(owner);

        DavAce allAllow = new DavAce.AllAce();
        allAllow.getPrivileges().add(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET);
        allAllow.setProtected(true);
        acl.getAces().add(allAllow);

        DavAce allDeny = new DavAce.AllAce();
        allDeny.setDenied(true);
        allDeny.getPrivileges().add(DavPrivilege.ALL);
        allDeny.setProtected(true);
        acl.getAces().add(allDeny);

        return acl;
    }

    /**
     * <p>
     * Extends the superclass method to return {@link DavPrivilege#READ} if
     * the the current principal is the non-admin user represented by this
     * resource.
     * </p>
     */
    protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
        Set<DavPrivilege> privileges = super.getCurrentPrincipalPrivileges();
        if (! privileges.isEmpty())
            return privileges;

        User user = getSecurityManager().getSecurityContext().getUser();
        if (user != null && user.equals(this.user))
            privileges.add(DavPrivilege.READ);

        return privileges;
    }
    
    protected void loadLiveProperties(DavPropertySet properties) {
        properties.add(new CreationDate(user.getCreationDate()));
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
        properties.add(new Etag(user.getEntityTag()));
        properties.add(new LastModified(user.getModifiedDate()));
        properties.add(new CalendarHomeSet(getResourceLocator(), user));
        
        // for now scheduling is an option
        if(isSchedulingEnabled()) {
            properties.add(new CalendarUserAddressSet(getResourceLocator(), user));
            properties.add(new ScheduleInboxURL(getResourceLocator(), user));
            properties.add(new ScheduleOutboxURL(getResourceLocator(), user));
        }
        
        properties.add(new AlternateUriSet(getResourceLocator(), user));
        properties.add(new PrincipalUrl(getResourceLocator(), user));
        properties.add(new GroupMembership(getResourceLocator(), user));
    }

    protected void setLiveProperty(DavProperty property)
        throws DavException {
        throw new ProtectedPropertyModificationException(property.getName());
    }

    protected void removeLiveProperty(DavPropertyName name)
        throws DavException {
        throw new ProtectedPropertyModificationException(name);
    }

    protected void loadDeadProperties(DavPropertySet properties) {
    }

    protected void setDeadProperty(DavProperty property)
        throws DavException {
        throw new ForbiddenException("Dead properties are not supported on this resource");
    }

    protected void removeDeadProperty(DavPropertyName name)
        throws DavException {
        throw new ForbiddenException("Dead properties are not supported on this resource");
    }

    private void writeHtmlRepresentation(OutputContext context)
        throws DavException, IOException {
        if (log.isDebugEnabled())
            log.debug("writing html representation for user principal " +
                      getDisplayName());

        context.setContentType(IOUtil.buildContentType("text/html", "UTF-8"));
        context.setModificationTime(getModificationTime());
        context.setETag(getETag());

        if (! context.hasStream()) {
            return;
        }

        PrintWriter writer =
            new PrintWriter(new OutputStreamWriter(context.getOutputStream(),
                                                   "utf8"));

        writer.write("<html>\n<head><title>");
        writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
        writer.write("</title></head>\n");
        writer.write("<body>\n");
        writer.write("<h1>");
        writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
        writer.write("</h1>\n");

        writer.write("<h2>Properties</h2>\n");
        writer.write("<dl>\n");
        for (DavPropertyIterator i=getProperties().iterator(); i.hasNext();) {
            DavProperty prop = (DavProperty) i.nextProperty();
            Object value = prop.getValue();
            String text = null;
            if (value instanceof Element) {
                try {
                    text = DomWriter.write((Element)value);
                } catch (Exception e) {
                    log.warn("Error serializing value for property " + prop.getName());
                }
            }
            if (text == null)
                text = prop.getValueText();
            writer.write("<dt>");
            writer.write(StringEscapeUtils.escapeHtml(prop.getName().toString()));
            writer.write("</dt><dd>");
            writer.write(StringEscapeUtils.escapeHtml(text));
            writer.write("</dd>\n");
        }
        writer.write("</dl>\n");

        DavResource parent = getParent();
        writer.write("<a href=\"");
        writer.write(parent.getResourceLocator().getHref(true));
        writer.write("\">");
        writer.write(StringEscapeUtils.escapeHtml(parent.getDisplayName()));
        writer.write("</a></li>\n");
        
        User user = getSecurityManager().getSecurityContext().getUser();
        if (user != null) {
            writer.write("<p>\n");
            DavResourceLocator homeLocator =
                getResourceLocator().getFactory().
                createHomeLocator(getResourceLocator().getContext(), user);
            writer.write("<a href=\"");
            writer.write(homeLocator.getHref(true));
            writer.write("\">");
            writer.write("Home collection");
            writer.write("</a><br>\n");
        }

        writer.write("</body>");
        writer.write("</html>\n");
        writer.close();
    }
}

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
package org.osaf.cosmo;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.*;
import org.osaf.cosmo.model.mock.MockEntityFactory;
import org.osaf.cosmo.security.mock.MockAnonymousPrincipal;
import org.osaf.cosmo.security.mock.MockUserPrincipal;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.security.Principal;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

/**
 */
public class TestHelper {
    private static final Log log = LogFactory.getLog(TestHelper.class);

    protected static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();

    protected static CalendarBuilder calendarBuilder = new CalendarBuilder();

    static int apseq = 0;
    static int cseq = 0;
    static int eseq = 0;
    static int iseq = 0;
    static int lseq = 0;
    static int pseq = 0;
    static int rseq = 0;
    static int sseq = 0;
    static int tseq = 0;
    static int useq = 0;

    private EntityFactory entityFactory = new MockEntityFactory();

    public TestHelper() {
    }

    public TestHelper(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public Calendar makeDummyCalendar() {
        Calendar cal =new Calendar();

        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);

        return cal;
    }

    public Calendar makeDummyCalendarWithEvent() {
        Calendar cal = makeDummyCalendar();

        VEvent e1 = makeDummyEvent();
        cal.getComponents().add(e1);

        VTimeZone tz1 = TimeZoneRegistryFactory.getInstance().createRegistry().
        getTimeZone("America/Los_Angeles").getVTimeZone();
        cal.getComponents().add(tz1);

        return cal;
    }

    public VEvent makeDummyEvent() {
        String serial = Integer.toString(++eseq);
        String summary = "dummy" + serial;

        // tomorrow
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.add(java.util.Calendar.DAY_OF_MONTH, 1);
        start.set(java.util.Calendar.HOUR_OF_DAY, 9);
        start.set(java.util.Calendar.MINUTE, 30);

        // 1 hour duration
        var duration = Duration.of(1, HOURS);

        VEvent event = new VEvent(new Date(start.getTime()), duration, summary);
        event.getProperties().add(new Uid(serial));

        // add timezone information
        VTimeZone tz = TimeZoneRegistryFactory.getInstance().createRegistry().
            getTimeZone("America/Los_Angeles").getVTimeZone();
        String tzValue =
            tz.getProperties().<TzId>getProperty(Property.TZID).getValue();
        net.fortuna.ical4j.model.parameter.TzId tzParam =
            new net.fortuna.ical4j.model.parameter.TzId(tzValue);
        event.getProperties().<DtStart>getProperty(Property.DTSTART).
            getParameters().add(tzParam);

        // add an alarm for 5 minutes before the event with an xparam
        // on the description
        var trigger = Duration.of( -5, MINUTES);
        VAlarm alarm = new VAlarm(trigger);
        alarm.getProperties().add(Action.DISPLAY);
        Description description = new Description("Meeting at 9:30am");
        XParameter xparam = new XParameter("X-COSMO-TEST-PARAM", "deadbeef");
        description.getParameters().add(xparam);
        alarm.getProperties().add(description);
        alarm.getProperties().add(new Description("Meeting at 9:30am"));
        event.getAlarms().add(alarm);

        // add an x-property with an x-param
        XProperty xprop = new XProperty("X-COSMO-TEST-PROP", "abc123");
        xprop.getParameters().add(xparam);
        event.getProperties().add(xprop);

        return event;
    }

    /** */
    public User makeDummyUser(String username,
                              String password) {
        if (username == null)
            throw new IllegalArgumentException("username required");
        if (password == null)
            throw new IllegalArgumentException("password required");

        User user = entityFactory.createUser();
        user.setUsername(username);
        user.setFirstName(username);
        user.setLastName(username);
        user.setEmail(username + "@localhost");
        user.setPassword(password);

        return user;
    }

    /** */
    public User makeDummyUser() {
        String serial = Integer.toString(++useq);
        String username = "dummy" + serial;
        return makeDummyUser(username, username);
    }

    /**
     */
    public Principal makeDummyUserPrincipal() {
        return new MockUserPrincipal(makeDummyUser());
    }

    /**
     */
    public Principal makeDummyUserPrincipal(String name,
                                            String password) {
        return new MockUserPrincipal(makeDummyUser(name, password));
    }

    /**
     */
    public Principal makeDummyUserPrincipal(User user) {
        return new MockUserPrincipal(user);
    }

    /**
     */
    public Principal makeDummyAnonymousPrincipal() {
        String serial = Integer.toString(++apseq);
        return new MockAnonymousPrincipal("dummy" + serial);
    }

    /**
     */
    public Principal makeDummyRootPrincipal() {
        User user = makeDummyUser();
        user.setAdmin(Boolean.TRUE);
        return new MockUserPrincipal(user);
    }

    /**
     */
    public Document loadXml(String name)
        throws Exception {
        InputStream in = getInputStream(name);
        BUILDER_FACTORY.setNamespaceAware(true);
        DocumentBuilder docBuilder = BUILDER_FACTORY.newDocumentBuilder();
        return docBuilder.parse(in);
    }

    public Calendar loadIcs(String name) throws Exception{
        InputStream in = getInputStream(name);
        return calendarBuilder.build(in);
    }

    /** */
    public ContentItem makeDummyContent(User user) {
        String serial = Integer.toString(++cseq);
        String name = "test content " + serial;

        FileItem content = entityFactory.createFileItem();

        content.setUid(name);
        content.setName(name);
        content.setOwner(user);
        content.setContent("test!".getBytes());
        content.setContentEncoding("UTF-8");
        content.setContentLanguage("en_US");
        content.setContentType("text/plain");

        return content;
    }

    public NoteItem makeDummyItem(User user) {
        return makeDummyItem(user, null);
    }

    public NoteItem makeDummyItem(User user,
                                  String name) {
        String serial = Integer.toString(++iseq);
        if (name == null)
            name = "test item " + serial;

        NoteItem note = entityFactory.createNote();

        note.setUid(name);
        note.setName(name);
        note.setOwner(user);
        note.setIcalUid(serial);
        note.setBody("This is a note. I love notes.");

        return note;
    }

    /** */
    public CollectionItem makeDummyCollection(User user) {
        String serial = Integer.toString(++lseq);
        String name = "test collection " + serial;

        CollectionItem collection = entityFactory.createCollection();
        collection.setUid(serial);
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(user);

        return collection;
    }

    public CollectionItem makeDummyCalendarCollection(User user) {
        return makeDummyCalendarCollection(user, null);
    }

    public CollectionItem makeDummyCalendarCollection(User user,
                                                      String name) {
        String serial = Integer.toString(++lseq);
        if (name == null)
            name = "test calendar collection " + serial;

        CollectionItem collection = entityFactory.createCollection();
        collection.setUid(serial);
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(user);

        collection.addStamp(entityFactory.createCalendarCollectionStamp(collection));

        return collection;
    }

    /** */
    public InputStream getInputStream(String name){
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        return in;
    }

    /** */
    public byte[] getBytes(String name) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(in, bos);
        return bos.toByteArray();
    }

    /** */
    public Reader getReader(String name) {
        return getReader(name, Charset.forName("UTF-8"));
    }

    public Reader getReader(String name, Charset cs) {
        try {
            byte[] buf = IOUtils.toByteArray(getInputStream(name));
            return new StringReader(new String(buf, cs));
        } catch (IOException e) {
            throw new RuntimeException("error converting input stream to reader", e);
        }
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }


}

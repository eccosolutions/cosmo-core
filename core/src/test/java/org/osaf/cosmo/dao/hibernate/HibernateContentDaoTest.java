/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.hibernate;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.property.ProdId;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.*;
import org.osaf.cosmo.model.hibernate.*;
import org.osaf.cosmo.xml.DomWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import javax.validation.ConstraintViolationException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.util.*;

public class HibernateContentDaoTest extends AbstractHibernateDaoTestCase {

    @Autowired
    protected UserDaoImpl userDao;

    @Autowired
    protected ContentDaoImpl contentDao;

    public HibernateContentDaoTest() {
        super();
    }

    @Test
    public void testContentDaoCreateContent() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        item.setName("test");

        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);
    }

    @Test
    public void testContentDaoLoadChildren() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        item.setName("test");

        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        clearSession();

        Set<ContentItem> children = contentDao.loadChildren(root, null);
        Assert.assertEquals(1, children.size());

        children = contentDao.loadChildren(root, newItem.getModifiedDate());
        Assert.assertEquals(0, children.size());

        children = contentDao.loadChildren(root, new Date(newItem.getModifiedDate().getTime() -1));
        Assert.assertEquals(1, children.size());
    }

    @Test
    public void testContentDaoCreateContentDuplicateUid() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item1 = generateTestContent();
        item1.setName("test");
        item1.setUid("uid");

        contentDao.createContent(root, item1);

        ContentItem item2 = generateTestContent();
        item2.setName("test2");
        item2.setUid("uid");

        try {
            contentDao.createContent(root, item2);
            clearSession();
            Assert.fail("able to create duplicate uid");
        } catch (UidInUseException ignored) {
        }
    }

    @Test
    public void testContentDaoCreateNoteDuplicateIcalUid() {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        NoteItem note1 = generateTestNote("note1", "testuser");
        note1.setIcalUid("icaluid");

        contentDao.createContent(root, note1);

        NoteItem note2 = generateTestNote("note2", "testuser");
        note2.setIcalUid("icaluid");


        try {
            contentDao.createContent(root, note2);
            Assert.fail("able to create duplicate icaluid");
        } catch (IcalUidInUseException ignored) {}

    }

    @Test
    public void testContentDaoInvalidContentEmptyName() throws Exception {

        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);
        ContentItem item = generateTestContent();
        item.setName("");

        try {
            contentDao.createContent(root, item);
            Assert.fail("able to create invalid content.");
        } catch (ConstraintViolationException e) {
            Assert.assertEquals("name", e.getConstraintViolations().iterator().next().getPropertyPath().toString());
        }
    }

    @Test
    public void testContentAttributes() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        IntegerAttribute ia = new HibIntegerAttribute(new HibQName("intattribute"), 22L);
        item.addAttribute(ia);
        BooleanAttribute ba = new HibBooleanAttribute(new HibQName("booleanattribute"), Boolean.TRUE);
        item.addAttribute(ba);

        DecimalAttribute decAttr =
            new HibDecimalAttribute(new HibQName("decimalattribute"),new BigDecimal("1.234567"));
        item.addAttribute(decAttr);

        // TODO: figure out db date type is handled because i'm seeing
        // issues with accuracy
        // item.addAttribute(new DateAttribute("dateattribute", new Date()));

        HashSet<String> values = new HashSet<>();
        values.add("value1");
        values.add("value2");
        MultiValueStringAttribute mvs = new HibMultiValueStringAttribute(new HibQName("multistringattribute"), values);
        item.addAttribute(mvs);

        HashMap<String, String> dictionary = new HashMap<>();
        dictionary.put("key1", "value1");
        dictionary.put("key2", "value2");
        DictionaryAttribute da = new HibDictionaryAttribute(new HibQName("dictionaryattribute"), dictionary);
        item.addAttribute(da);

        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("decimalattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof DecimalAttribute);
        Assert.assertEquals(attr.getValue().toString(),"1.234567");

        @SuppressWarnings("unchecked")
		Set<String> querySet = (Set<String>) queryItem
                .getAttributeValue("multistringattribute");
        Assert.assertTrue(querySet.contains("value1"));
        Assert.assertTrue(querySet.contains("value2"));

        @SuppressWarnings("unchecked")
		Map<String, String> queryDictionary = (Map<String, String>) queryItem
                .getAttributeValue("dictionaryattribute");
        Assert.assertEquals("value1", queryDictionary.get("key1"));
        Assert.assertEquals("value2", queryDictionary.get("key2"));

        Attribute custom = queryItem.getAttribute("customattribute");
        Assert.assertEquals("customattributevalue", custom.getValue());

        helper.verifyItem(newItem, queryItem);

        // set attribute value to null
        custom.setValue(null);

        querySet.add("value3");
        queryDictionary.put("key3", "value3");

        queryItem.removeAttribute("intattribute");

        contentDao.updateContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        querySet = (Set) queryItem.getAttributeValue("multistringattribute");
        queryDictionary = (Map) queryItem
                .getAttributeValue("dictionaryattribute");
        Attribute queryAttribute = queryItem.getAttribute("customattribute");

        Assert.assertTrue(querySet.contains("value3"));
        Assert.assertEquals("value3", queryDictionary.get("key3"));
        Assert.assertNotNull(queryAttribute);
        Assert.assertNull(queryAttribute.getValue());
        Assert.assertNull(queryItem.getAttribute("intattribute"));
    }

    @Ignore("Timezone seems to be GMT+1 when testing") // And we've removed @Entity mapping for HibCalendarAttribute
    @Test
    public void junit3ignored_testCalendarAttribute() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        CalendarAttribute calAttr =
            new HibCalendarAttribute(new HibQName("calendarattribute"), "2002-10-10T00:00:00+05:00");
        item.addAttribute(calAttr);

        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("calendarattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof CalendarAttribute);

        Calendar cal = (Calendar) attr.getValue();
        Assert.assertEquals(cal.getTimeZone().getID(), "GMT+05:00");
        Assert.assertEquals(cal, calAttr.getValue()); // fails: out by 4 hours

        attr.setValue("2003-10-10T00:00:00+02:00");

        contentDao.updateContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Attribute queryAttr = queryItem.getAttribute(new HibQName("calendarattribute"));
        Assert.assertNotNull(queryAttr);
        Assert.assertTrue(queryAttr instanceof CalendarAttribute);

        cal = (Calendar) queryAttr.getValue();
        Assert.assertEquals(cal.getTimeZone().getID(), "GMT+02:00");
        Assert.assertEquals(cal, attr.getValue()); // fails: 1 hour out (something is using GMT+1 ...
    }

    @Test
    public void testTimestampAttribute() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        Date dateVal = new Date();
        TimestampAttribute tsAttr =
            new HibTimestampAttribute(new HibQName("timestampattribute"), dateVal);
        item.addAttribute(tsAttr);

        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("timestampattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof TimestampAttribute);

        Date val = (Date) attr.getValue();
        Assert.assertEquals(dateVal, val);

        dateVal.setTime(dateVal.getTime() + 101);
        attr.setValue(dateVal);

        contentDao.updateContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Attribute queryAttr = queryItem.getAttribute(new HibQName("timestampattribute"));
        Assert.assertNotNull(queryAttr);
        Assert.assertTrue(queryAttr instanceof TimestampAttribute);

        val = (Date) queryAttr.getValue();
        Assert.assertEquals(dateVal, val);
    }

//    @Ignore("FIXME: fails since updateContent(queryItem) updates modifiedDate")
    @Test
    public void testXmlAttribute() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        Element testElement = createTestElement();
        Element testElement2 = createTestElement();

        testElement2.setAttribute("foo", "bar");

        Assert.assertFalse(testElement.isEqualNode(testElement2));

        XmlAttribute xmlAttr =
            new HibXmlAttribute(new HibQName("xmlattribute"), testElement );
        item.addAttribute(xmlAttr);

        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("xmlattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof XmlAttribute);

        Element element = (Element) attr.getValue();

        Assert.assertEquals(DomWriter.write(testElement), DomWriter.write(element));

        Date modifyDate = attr.getModifiedDate();

        // Sleep a couple millis to make sure modifyDate doesn't change
        Thread.sleep(1000);

        contentDao.updateContent(queryItem);

        // DIRTIES the session - therefore updates modifiedDate and the test still fails
        // it appears to be the 'value' on HibXmlAttribute, which is backed up by this:
        //  "HHH000481: Encountered Java type for which we could not locate a JavaTypeDescriptor and which does not appear
        //  to implement equals and/or hashCode. This can lead to significant performance problems when performing
        //  equality/dirty checking involving this Java type. Consider registering a custom JavaTypeDescriptor
        //  or at least implementing equals/hashCode."
        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        // this wants to see the modified date unchanged - but its updated above
        attr = queryItem.getAttribute(new HibQName("xmlattribute"));

        // Attribute shouldn't have been updated
        Assert.assertEquals(modifyDate, attr.getModifiedDate());

        attr.setValue(testElement2);

        // Sleep a couple millis to make sure modifyDate doesn't change
        Thread.sleep(2);
        modifyDate = attr.getModifiedDate();

        contentDao.updateContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        attr = queryItem.getAttribute(new HibQName("xmlattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof XmlAttribute);
        // Attribute should have been updated
        Assert.assertTrue(modifyDate.before(attr.getModifiedDate()));

        element = (Element) attr.getValue();

        Assert.assertEquals(DomWriter.write(testElement2),DomWriter.write(element));
    }

    @Test
    public void testICalendarAttribute() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ICalendarAttribute icalAttr = new HibICalendarAttribute();
        icalAttr.setQName(new HibQName("icalattribute"));
        icalAttr.setValue(helper.getInputStream("vjournal.ics"));
        item.addAttribute(icalAttr);

        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("icalattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof ICalendarAttribute);

        net.fortuna.ical4j.model.Calendar calendar = (net.fortuna.ical4j.model.Calendar) attr.getValue();
        Assert.assertNotNull(calendar);

        net.fortuna.ical4j.model.Calendar expected = CalendarUtils.parseCalendar(helper.getInputStream("vjournal.ics"));

        Assert.assertEquals(expected.toString(),calendar.toString());

        calendar.getProperties().add(new ProdId("blah"));

        contentDao.updateContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        ICalendarAttribute ica = (ICalendarAttribute) queryItem.getAttribute(new HibQName("icalattribute"));
        Assert.assertNotEquals(expected.toString(), ica.getValue().toString());
    }

    @Test
    public void testCreateDuplicateRootItem() {
        User testuser = getUser(userDao, "testuser");
        try {
            contentDao.createRootItem(testuser);
            Assert.fail("able to create duplicate root item");
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void testFindItem() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");

        CollectionItem root = contentDao
                .getRootItem(testuser2);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        clearSession();

        Item queryItem = contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        queryItem = contentDao.findItemByPath("/testuser2/a");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        ContentItem item = generateTestContent();

        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        item = contentDao.createContent(a, item);

        clearSession();

        queryItem = contentDao.findItemByPath("/testuser2/a/test");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);

        clearSession();

        queryItem = contentDao.findItemParentByPath("/testuser2/a/test");
        Assert.assertNotNull(queryItem);
        Assert.assertEquals(a.getUid(), queryItem.getUid());
    }

    @Test
    public void testContentDaoUpdateContent() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        FileItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);
        Date newItemModifyDate = newItem.getModifiedDate();

        clearSession();

        HibFileItem queryItem = (HibFileItem) contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);
        Assert.assertEquals(0, queryItem.getVersion().intValue());

        queryItem.setName("test2");
        queryItem.setDisplayName("this is a test item2");
        queryItem.removeAttribute("customattribute");
        queryItem.setContentLanguage("es");
        queryItem.setContent(helper.getBytes("testdata2.txt"));

        // Make sure modified date changes
        Thread.sleep(1000);

        queryItem = (HibFileItem) contentDao.updateContent(queryItem);

        clearSession();
        Thread.sleep(200);
        HibContentItem queryItem2 = (HibContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertTrue(queryItem2.getVersion() > 0);

        helper.verifyItem(queryItem, queryItem2);

        Assert.assertTrue(newItemModifyDate.before(
                queryItem2.getModifiedDate()));
    }

    @Test
    public void testContentDaoDeleteContent() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);

        clearSession();

        root = contentDao.getRootItem(user);
        Assert.assertEquals(0, root.getChildren().size());

    }

    @Test
    public void testContentDaoDeleteUserContent() throws Exception {
        User user1 = getUser(userDao, "testuser1");
        User user2 = getUser(userDao, "testuser2");
        CollectionItem root = contentDao.getRootItem(user1);

        // Create test content, with owner of user2
        ContentItem item = generateTestContent();
        item.setOwner(user2);

        // create content in user1's home collection
        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        user1 = getUser(userDao, "testuser1");
        user2 = getUser(userDao, "testuser2");

        // remove user2's content, which should include the item created
        // in user1's home collections
        contentDao.removeUserContent(user2);

        root = contentDao.getRootItem(user1);
        Assert.assertEquals(0, root.getChildren().size());
    }

    @Test
    public void testDeleteContentByPath() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeItemByPath("/testuser/test");

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
    }

    @Test
    public void testDeleteContentByUid() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeItemByUid(queryItem.getUid());

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
    }

    @Test
    public void testTombstoneDeleteContent() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        Assert.assertEquals(0, (int) ((HibItem) queryItem).getVersion());

        contentDao.removeContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(queryItem);

        root = contentDao.getRootItem(user);
        Assert.assertEquals(root.getTombstones().size(), 1);

        Tombstone ts = root.getTombstones().iterator().next();

        Assert.assertTrue(ts instanceof ItemTombstone);
        Assert.assertEquals(((ItemTombstone) ts).getItemUid(), newItem.getUid());

        item = generateTestContent();
        item.setUid(newItem.getUid());

        contentDao.createContent(root, item);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Assert.assertNotNull(queryItem);

        root = contentDao.getRootItem(user);
        Assert.assertEquals(root.getTombstones().size(), 0);
    }

    @Test
    public void testContentDaoCreateCollection() throws Exception {
        User user = getUser(userDao, "testuser2");
        CollectionItem root = contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);
        a.setHue(1L);

        a = contentDao.createCollection(root, a);

        Assert.assertTrue(getHibItem(a).getId() > -1);
        Assert.assertNotNull(a.getUid());

        clearSession();

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assertions.assertThat(queryItem.getHue()).isEqualTo(1);
        helper.verifyItem(a, queryItem);
    }

    @Test
    public void testContentDaoUpdateCollection() throws Exception {
        User user = getUser(userDao, "testuser2");
        CollectionItem root = contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        clearSession();

        Assert.assertTrue(getHibItem(a).getId() > -1);
        Assert.assertNotNull(a.getUid());

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        helper.verifyItem(a, queryItem);

        queryItem.setName("b");
        contentDao.updateCollection(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertEquals("b", queryItem.getName());
    }

    @Test
    public void testContentDaoUpdateCollectionTimestamp() throws Exception {
        contentDao.setShouldUpdateCollectionTimestamp(true); // See javadoc

        User user = getUser(userDao, "testuser2");
        CollectionItem root = contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);
        Integer ver = ((HibItem) a).getVersion();
        Date timestamp = a.getModifiedDate();

        clearSession();
        Thread.sleep(1);

        a = contentDao.updateCollectionTimestamp(a);
        Assert.assertEquals((int) ((HibItem) a).getVersion(), ver + 1);
        Assert.assertTrue(timestamp.before(a.getModifiedDate()));
    }

    @Test
    public void testContentDaoDeleteCollection() {
        User user = getUser(userDao, "testuser2");
        CollectionItem root = contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        clearSession();

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);

        contentDao.removeCollection(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNull(queryItem);
    }

    @Test
    public void testContentDaoAdvanced() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        CollectionItem root = contentDao
                .getRootItem(testuser2);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(getUser(userDao, "testuser2"));

        b = contentDao.createCollection(a, b);

        ContentItem c = generateTestContent("c", "testuser2");

        c = contentDao.createContent(b, c);

        ContentItem d = generateTestContent("d", "testuser2");

        d = contentDao.createContent(a, d);

        clearSession();

        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        c = (ContentItem) contentDao.findItemByUid(c.getUid());
        d = (ContentItem) contentDao.findItemByUid(d.getUid());
        root = contentDao.getRootItem(testuser2);

        Assert.assertNotNull(a);
        Assert.assertNotNull(b);
        Assert.assertNotNull(d);
        Assert.assertNotNull(root);

        // test children
        Collection children = a.getChildren();
        Assert.assertEquals(2, children.size());
        verifyContains(children, b);
        verifyContains(children, d);

        children = root.getChildren();
        Assert.assertEquals(1, children.size());
        verifyContains(children, a);

        // test get by path
        ContentItem queryC = (ContentItem) contentDao.findItemByPath("/testuser2/a/b/c");
        Assert.assertNotNull(queryC);
        helper.verifyInputStream(
                helper.getInputStream("testdata1.txt"), ((FileItem) queryC)
                        .getContent());
        Assert.assertEquals("c", queryC.getName());

        // test get path/uid abstract
        Item queryItem = contentDao.findItemByPath("/testuser2/a/b/c");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);

        queryItem = contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        // test delete
        contentDao.removeContent(c);
        queryC = (ContentItem) contentDao.findItemByUid(c.getUid());
        Assert.assertNull(queryC);

        contentDao.removeCollection(a);

        CollectionItem queryA = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNull(queryA);

        ContentItem queryD = (ContentItem) contentDao.findItemByUid(d.getUid());
        Assert.assertNull(queryD);
    }


    @Test
    public void testHomeCollection() {
        User testuser2 = getUser(userDao, "testuser2");
        HomeCollectionItem root = contentDao.getRootItem(testuser2);

        Assert.assertNotNull(root);
        root.setName("alsfjal;skfjasd");
        Assert.assertEquals(root.getName(), "testuser2");

    }

    @Test
    public void testItemDaoMove() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        CollectionItem root = contentDao
                .getRootItem(testuser2);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(getUser(userDao, "testuser2"));

        b = contentDao.createCollection(a, b);

        CollectionItem c = new HibCollectionItem();
        c.setName("c");
        c.setOwner(getUser(userDao, "testuser2"));

        c = contentDao.createCollection(b, c);

        ContentItem d = generateTestContent("d", "testuser2");

        d = contentDao.createContent(c, d);

        CollectionItem e = new HibCollectionItem();
        e.setName("e");
        e.setOwner(getUser(userDao, "testuser2"));

        e = contentDao.createCollection(a, e);

        clearSession();

        root = contentDao.getRootItem(testuser2);
        e = (CollectionItem) contentDao.findItemByUid(e.getUid());
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());

        // verify can't move root collection
        try {
            contentDao.moveItem("/testuser2", "/testuser2/a/blah");
            Assert.fail("able to move root collection");
        } catch (IllegalArgumentException ignored) {
        }

        // verify can't move to root collection
        try {
            contentDao.moveItem("/testuser2/a/e", "/testuser2");
            Assert.fail("able to move to root collection");
        } catch (ItemNotFoundException ignored) {
        }

        // verify can't create loop
        try {
            contentDao.moveItem("/testuser2/a/b", "/testuser2/a/b/c/new");
            Assert.fail("able to create loop");
        } catch (ModelValidationException ignored) {
        }

        clearSession();

        // verify that move works
        b = (CollectionItem) contentDao.findItemByPath("/testuser2/a/b");

        contentDao.moveItem("/testuser2/a/b", "/testuser2/a/e/b");

        clearSession();

        CollectionItem queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/b");
        Assert.assertNotNull(queryCollection);

        contentDao.moveItem("/testuser2/a/e/b", "/testuser2/a/e/bnew");

        clearSession();
        queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bnew");
        Assert.assertNotNull(queryCollection);

        Item queryItem = contentDao.findItemByPath("/testuser2/a/e/bnew/c/d");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);
    }

    @Test
    public void testItemDaoCopy() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        CollectionItem root = contentDao
                .getRootItem(testuser2);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(getUser(userDao, "testuser2"));

        b = contentDao.createCollection(a, b);

        CollectionItem c = new HibCollectionItem();
        c.setName("c");
        c.setOwner(getUser(userDao, "testuser2"));

        c = contentDao.createCollection(b, c);

        ContentItem d = generateTestContent("d", "testuser2");

        d = contentDao.createContent(c, d);

        CollectionItem e = new HibCollectionItem();
        e.setName("e");
        e.setOwner(getUser(userDao, "testuser2"));

        e = contentDao.createCollection(a, e);

        clearSession();

        root = contentDao.getRootItem(testuser2);
        e = (CollectionItem) contentDao.findItemByUid(e.getUid());
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());

        // verify can't copy root collection
        try {
            contentDao.copyItem(root, "/testuser2/a/blah", true);
            Assert.fail("able to copy root collection");
        } catch (IllegalArgumentException ignored) {
        }

        // verify can't move to root collection
        try {
            contentDao.copyItem(e, "/testuser2", true);
            Assert.fail("able to move to root collection");
        } catch (ItemNotFoundException ignored) {
        }

        // verify can't create loop
        try {
            contentDao.copyItem(b, "/testuser2/a/b/c/new", true);
            Assert.fail("able to create loop");
        } catch (ModelValidationException ignored) {
        }

        clearSession();

        // verify that copy works
        b = (CollectionItem) contentDao.findItemByPath("/testuser2/a/b");

        contentDao.copyItem(b, "/testuser2/a/e/bcopy", true);

        clearSession();

        CollectionItem queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopy");
        Assert.assertNotNull(queryCollection);

        queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopy/c");
        Assert.assertNotNull(queryCollection);

        d = (ContentItem) contentDao.findItemByUid(d.getUid());
        ContentItem dcopy = (ContentItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopy/c/d");
        Assert.assertNotNull(dcopy);
        Assert.assertEquals(d.getName(), dcopy.getName());
        Assert.assertNotSame(d.getUid(), dcopy.getUid());
        helper.verifyBytes(((FileItem) d).getContent(), ((FileItem) dcopy).getContent());

        clearSession();

        b = (CollectionItem) contentDao.findItemByPath("/testuser2/a/b");

        contentDao.copyItem(b,"/testuser2/a/e/bcopyshallow", false);

        clearSession();

        queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopyshallow");
        Assert.assertNotNull(queryCollection);

        queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopyshallow/c");
        Assert.assertNull(queryCollection);

        clearSession();
        d = (ContentItem) contentDao.findItemByUid(d.getUid());
        contentDao.copyItem(d,"/testuser2/dcopy", true);

        clearSession();

        dcopy = (ContentItem) contentDao.findItemByPath("/testuser2/dcopy");
        Assert.assertNotNull(dcopy);
    }

    @Test
    public void testItemInMutipleCollections() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        ContentItem item = generateTestContent();
        item.setName("test");

        ContentItem newItem = contentDao.createContent(a, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);

        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(user);

        b = contentDao.createCollection(root, b);

        contentDao.addItemToCollection(queryItem, b);

        clearSession();
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 2);

        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        contentDao.removeItemFromCollection(queryItem, b);
        clearSession();
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);

        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        contentDao.removeItemFromCollection(queryItem, a);
        clearSession();
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(queryItem);
    }

    @Test
    public void testItemInMutipleCollectionsError() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        ContentItem item = generateTestContent();
        item.setName("test");

        ContentItem newItem = contentDao.createContent(a, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);

        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(user);

        b = contentDao.createCollection(root, b);

        ContentItem item2 = generateTestContent();
        item2.setName("test");
        contentDao.createContent(b, item2);

        // should get DuplicateItemName here
        try {
            contentDao.addItemToCollection(queryItem, b);
            Assert.fail("able to add item with same name to collection");
        } catch (DuplicateItemNameException ignored) {
        }
    }

    @Test
    public void testItemInMutipleCollectionsDeleteCollection() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        ContentItem item = generateTestContent();
        item.setName("test");

        ContentItem newItem = contentDao.createContent(a, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);

        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(user);

        b = contentDao.createCollection(root, b);

        contentDao.addItemToCollection(queryItem, b);

        clearSession();
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 2);

        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        contentDao.removeCollection(b);

        clearSession();
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(b);
        Assert.assertEquals(queryItem.getParents().size(), 1);

        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        contentDao.removeCollection(a);
        clearSession();

        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(a);
        Assert.assertNull(queryItem);
    }


    @Test
    public void testContentDaoCreateFreeBusy() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        FreeBusyItem newItem = new HibFreeBusyItem();
        newItem.setOwner(user);
        newItem.setName("test");
        newItem.setIcalUid("icaluid");

        CalendarBuilder cb = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = cb.build(helper.getInputStream("vfreebusy.ics"));

        newItem.setFreeBusyCalendar(calendar);

        newItem = (FreeBusyItem) contentDao.createContent(root, newItem);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);
    }

    @Test
    public void testContentDaoCreateAvailability() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        AvailabilityItem newItem = new HibAvailabilityItem();
        newItem.setOwner(user);
        newItem.setName("test");
        newItem.setIcalUid("icaluid");

        CalendarBuilder cb = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = cb.build(helper.getInputStream("vavailability.ics"));

        newItem.setAvailabilityCalendar(calendar);

        newItem = (AvailabilityItem) contentDao.createContent(root, newItem);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);
    }

    @Test
    public void testContentDaoUpdateCollection2() {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        NoteItem note1 = generateTestNote("test1", "testuser");
        NoteItem note2 = generateTestNote("test2", "testuser");

        note1.setUid("1");
        note2.setUid("2");

        Set<ContentItem> items = new HashSet<>();
        items.add(note1);
        items.add(note2);

        contentDao.updateCollection(root, items);

        items.clear();

        note1 = (NoteItem) contentDao.findItemByUid("1");
        note2 = (NoteItem) contentDao.findItemByUid("2");

        items.add(note1);
        items.add(note2);

        Assert.assertNotNull(note1);
        Assert.assertNotNull(note2);

        note1.setDisplayName("changed");
        note2.setIsActive(false);

        contentDao.updateCollection(root, items);

        note1 = (NoteItem) contentDao.findItemByUid("1");
        note2 = (NoteItem) contentDao.findItemByUid("2");

        Assert.assertNotNull(note1);
        Assert.assertEquals("changed", note1.getDisplayName());
        Assert.assertNull(note2);
    }

    @Test
    public void testContentDaoUpdateCollectionWithMods() {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        NoteItem note1 = generateTestNote("test1", "testuser");
        NoteItem note2 = generateTestNote("test2", "testuser");

        note1.setUid("1");
        note2.setUid("1:20070101");

        note2.setModifies(note1);

        Set<ContentItem> items = new LinkedHashSet<>();
        items.add(note2);
        items.add(note1);


        // should fail because modification is processed before master
        try {
            contentDao.updateCollection(root, items);
            Assert.fail("able to create invalid mod");
        } catch (ModelValidationException ignored) {
        }

        items.clear();

        // now make sure master is processed before mod
        items.add(note1);
        items.add(note2);

        contentDao.updateCollection(root, items);

        note1 = (NoteItem) contentDao.findItemByUid("1");
        Assert.assertNotNull(note1);
        Assert.assertEquals(1, note1.getModifications().size());
        note2 = (NoteItem) contentDao.findItemByUid("1:20070101");
        Assert.assertNotNull(note2);
        Assert.assertNotNull(note2.getModifies());

        // now create new collection
        CollectionItem a = new HibCollectionItem();
        a.setUid("a");
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        // try to add mod to another collection before adding master
        items.clear();
        items.add(note2);

        // should fail because modification is added before master
        try {
            contentDao.updateCollection(a, items);
            Assert.fail("able to add mod before master");
        } catch (ModelValidationException ignored) {
        }

        items.clear();
        items.add(note1);
        items.add(note2);

        contentDao.updateCollection(a, items);

        // now create new collection
        CollectionItem b = new HibCollectionItem();
        b.setUid("b");
        b.setName("b");
        b.setOwner(user);

        b = contentDao.createCollection(root, b);

        // only add master
        items.clear();
        items.add(note1);

        contentDao.updateCollection(b, items);

        // adding master should add mods too
        clearSession();
        b = (CollectionItem) contentDao.findItemByUid("b");
        Assert.assertNotNull(b);
        Assert.assertEquals(2, b.getChildren().size());
    }

    @Test
    public void testContentDaoUpdateCollectionWithDuplicateIcalUids() {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        NoteItem note1 = generateTestNote("test1", "testuser");
        NoteItem note2 = generateTestNote("test2", "testuser");

        note1.setUid("1");
        note1.setIcalUid("1");
        note2.setUid("2");
        note2.setIcalUid("1");

        Set<ContentItem> items = new HashSet<>();
        items.add(note1);
        items.add(note2);

        try {
            contentDao.updateCollection(root, items);
            Assert.fail("able to create duplicate icaluids!");
        } catch (IcalUidInUseException ignored) {
        }
    }


    private void verifyContains(Collection items, CollectionItem collection) {
        for (Object o : items) {
            Item item = (Item) o;
            if (item instanceof CollectionItem
                && item.getName().equals(collection.getName()))
                return;
        }
        Assert.fail("collection not found");
    }

    private void verifyContains(Collection items, ContentItem content) {
        for (Object o : items) {
            Item item = (Item) o;
            if (item instanceof ContentItem
                && item.getName().equals(content.getName()))
                return;
        }
        Assert.fail("content not found");
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private FileItem generateTestContent() throws Exception {
        return generateTestContent("test", "testuser");
    }

    private FileItem generateTestContent(String name, String owner)
            throws Exception {
        FileItem content = new HibFileItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setContent(helper.getBytes("testdata1.txt"));
        content.setContentLanguage("en");
        content.setContentEncoding("UTF8");
        content.setContentType("text/text");
        content.setOwner(getUser(userDao, owner));
        content.addAttribute(new HibStringAttribute(new HibQName("customattribute"),
                "customattributevalue"));
        return content;
    }

    private NoteItem generateTestNote(String name, String owner) {
        NoteItem content = new HibNoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setOwner(getUser(userDao, owner));
        return content;
    }

    private Element createTestElement() throws Exception {
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element root = document.createElement( "root" );
        document.appendChild(root);
        {
            Element author = document.createElement("author");
            author.setAttribute("name", "James");
            author.setAttribute("location", "UK");
            author.setTextContent("James Strachan");
            root.appendChild(author);
        }
        {
            Element author = document.createElement("author");
            author.setAttribute("name", "Bob");
            author.setAttribute("location", "US");
            author.setTextContent("Bob McWhirter");
            root.appendChild(author);
        }

        return document.getDocumentElement();
    }

    private HibItem getHibItem(Item item) {
        return (HibItem) item;
    }

}
